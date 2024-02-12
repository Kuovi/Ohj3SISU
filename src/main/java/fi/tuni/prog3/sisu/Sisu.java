package fi.tuni.prog3.sisu;

import static fi.tuni.prog3.sisu.KoriAPIGetter.readModuleData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONObject;

import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.util.Callback;


/**
 * 
 * Class handles the UI elements of the program.
 */
public class Sisu extends Application {
    
    private static TableView<String> infoTable = new TableView<>();
    private static HashMap<String, String> treeCellIds = new HashMap<>(); 
    private String selectedCourseId;
    private static final TableView<JSONObject> degreeTable = new TableView<>();
    
    private static boolean idModeFlag = false;
    
    private void importDegrees() {

        JSONObject j = new JSONObject();
        j = KoriAPIGetter.getJSONFromAPI("https://sis-tu"
            + "ni.funidata.fi/kori/api/module-search?curriculumPeri"
            + "odId=uta-lvv-2021&universityId=tuni-university-root-"
            + "id&moduleType=DegreeProgramme&limit=1000");
        Database.addDegreeProgrammesToList(j);
    }
    
    /**
     * 
     * 
     */
    private void importProgram(String groupId){
            int i = 0;
            for (var json : Database.getDegreeProgrammes()) {
                if (json.get("groupId").equals(groupId)) {
                    readModuleData(Database.getDegreeProgrammes().get(i), null);
                    break;
                }
                i++;
            }
    }
    
    private TreeView<String> createNestedTree(String groupId) {
        Pair[] nestedDegree = createDegreeProgramNestedData(groupId);
        String degreeName = nestedDegree[0].getKey().toString();
        TreeMap<String, List<String>> data = 
                     (TreeMap<String, List<String>>) nestedDegree[0].getValue();

        // create the tree from the data.
        TreeView<String> tree = createTreeView(data,degreeName);

        return tree;
    }
    
    private Pair[] createDegreeProgramNestedData(String groupId) {
         String degreeName = "";
        TreeMap<String, List<String>> data = new TreeMap<>();  
        if (!Database.getDegreeProgrammeMap().isEmpty()) {
            DegreeProgramme dp = Database.getDegreeProgrammeMap().get(groupId);
            degreeName = dp.getName()+"$"+dp.getGroupId();
            data = readNestedDataFromModule(data, groupId);
        }
        return new Pair[] {new Pair <>(degreeName, data)};
    }
    
    private TreeMap<String, List<String>> readNestedDataFromModule
                          (TreeMap<String, List<String>> data, String groupId) {
        Module mod;
        mod = readClassType(groupId);
        if (mod != null) {
            String modName= mod.getName();

            TreeMap<String,ArrayList<String>> dpChilds = mod.getChildren();
            ArrayList<String> dpModuleChildrenNames = new ArrayList<>();
            for (String childId : dpChilds.get("moduleGroupId")) {
                mod = readClassType(childId);
                dpModuleChildrenNames.add(mod.getName()+"$"+mod.getGroupId());
            }
            ArrayList<String> dpCourseChildrenNames = new ArrayList<>();
            dpChilds.get("courseGroupId").stream().map(childId -> 
                           Database.getCourseMap().get(childId))
                           .forEachOrdered(course -> {dpCourseChildrenNames
                                 .add(course.getName()+"$"+course.getGroupId());
            });
            data.put(modName, Stream.of(dpModuleChildrenNames, 
                                        dpCourseChildrenNames)
                                        .flatMap(Collection::stream)
                                        .collect(Collectors.toList()));
            for (String childModuleId : dpChilds.get("moduleGroupId")) {
                data = readNestedDataFromModule(data, childModuleId);
            }
        }
        return data;
    }
    
    private Module readClassType(String id) {
        Module mod;
        if (Database.getDegreeProgrammeMap().get(id) != null) {
            mod = Database.getDegreeProgrammeMap().get(id);
        }
        else if (Database.getStudyModuleMap().get(id) != null) {
            mod = Database.getStudyModuleMap().get(id);
        }
        else if (Database.getGroupingModuleMap().get(id) != null) {
            mod = Database.getGroupingModuleMap().get(id);
        }
        else {
            mod = null;
        }
        return mod;
    }
    
    private CourseUnit checkIfCourse(String id) {
        CourseUnit course;
        if (Database.getCourseMap().get(id) != null) {
            course = Database.getCourseMap().get(id);
        }
        else {
            course = null;
        }
        return course;
    }
    
    /**
     * Create a TreeView of a set of data
     * given the data and identified roots within the data.
     */
    private TreeView<String> createTreeView(TreeMap<String, List<String>> data,
                                                    String degreeName) {
        
        TreeItem<String> root = new TreeItem<>();

        root.getChildren().add(createTreeItem(data, degreeName));
        TreeView<String> tree = new TreeView<>();
        tree.setCellFactory((var tree1) -> {
            TreeCell<String> cell = new TreeCell<String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty) ;
                    if (empty) {
                        setText(null);
                    } else {
                        setText(item);
                    }
                }
            };
            // Additional functionality: 
            // change cell to show id and back if idModeFlag is on.
            cell.setOnMouseClicked(event -> {
                if (! cell.isEmpty()) {
                    TreeItem<String> treeItem = cell.getTreeItem();
                    // id to name.
                    String treeItemValue = treeItem.getValue();
                    if (treeCellIds.values().contains(treeItem.getValue())) {
                        var entrySet = treeCellIds.entrySet();
                        for (Map.Entry<String, String> entry : entrySet  ) {
                            if ( entry.getValue().equals(treeItem.getValue())) {
                                tree.setEditable(true);
                                showInfo(treeItemValue);
                                selectedCourseId = treeItemValue;
                                if (idModeFlag) {
                                    tree.setEditable(true);
                                    treeItem.setValue(entry.getKey());
                                }
                            }
                        }
                    }
                    // name to id.
                    else if (treeItem.getValue().equals(treeItemValue)) {
                        String id = treeCellIds.get(treeItemValue);
                        showInfo(id);
                        selectedCourseId = id;
                        if (idModeFlag) {
                            tree.setEditable(true);
                            treeItem.setValue(id);
                        }
                    }
                }
            });
            tree.setEditable(false);
            return cell ;
        });
        tree.setPrefWidth(470);
        tree.setRoot(root);
        tree.setShowRoot(false);

        return tree;
    }

    private String fitContent(String content){
        int start = 0;
        content = content.replaceAll("<.*?>","");
       content = content.replace("\\n", "").replace("\\u", " ");
        content = content.replace("\\s+", "");
        content = content.replaceAll("[{,}]", "");
        int i = content.length()/45;
        ArrayList<String> arr = new ArrayList<>();
        for(int j = 0; j<=i; j++){
            if(j<i){
                arr.add(content.substring(start, start+45) +"\n");
            }
            else{arr.add(content.substring(start, content.length()) +"\n");}
            start +=45;
                   
        }
        String toShow = String.join(", ", arr);
        toShow = toShow.replaceAll("[{,}]", "");
        return toShow;
    }
    
    
    /*
     * Sets selected module or course info to the infoTable TableView.
     */
    private void showInfo(String id){
        String name = "";
        // Get info from Database maps with id
        //infoTable.setItems("INFO HERE");
        Collection<String> info_list = new ArrayList<>();
        
        
        // Shows info of a degreeprogramme
        if(Database.getDegreeProgrammeMap().containsKey(id)){
            
            infoTable.setFixedCellSize(59.0);
            var degprog = Database.getDegreeProgrammeMap().get(id);
            info_list.add("Degreeprogram code: " + degprog.code);
            info_list.add("Target credits: " + String.valueOf(degprog.getTargetCredits()));

            name = degprog.getName();
            // Set name to column header.
            infoTable.getColumns().forEach(c -> {
                Label l = new Label(degprog.getName());
                c.setGraphic(l);   
            });
        }
        
        //shows info of studymodule 
        else if(Database.getStudyModuleMap().containsKey(id)){
            var studMod = Database.getStudyModuleMap().get(id);
            info_list.add("Modulecode: " + studMod.code);
            info_list.add("Target credits: " + String.valueOf(studMod.getTargetCredits()));
            if(!studMod.getContentDescription().equals("null")){
                String content = studMod.getContentDescription();
                String toShow = fitContent(content);
                info_list.add("ContentDescription:\n" + toShow);
                infoTable.setFixedCellSize(0);
            }
            info_list.add("Study-level: " + studMod.getStudyLevel().replace("urn:code:study-level:", ""));
            name = studMod.getName();
            // Set name to column header.
            infoTable.getColumns().forEach(c -> {
                Label l = new Label(studMod.getName());
                c.setGraphic(l);   
            });
        }
        
        // shows info of groupingmodule, which doesnt contain any useful information
        else if(Database.getGroupingModuleMap().containsKey(id)){
            infoTable.setFixedCellSize(59.0);
            var groupMod = Database.getGroupingModuleMap().get(id);
            info_list.add("No exra info on this module");  
            name = groupMod.getName();
            // Set name to column header.
            infoTable.getColumns().forEach(c -> {
                Label l = new Label(groupMod.getName());
                c.setGraphic(l);   
            });
        
        }
        
        //shows info of course
        else if(Database.getCourseMap().containsKey(id)){
            var courmod = Database.getCourseMap().get(id);
            if(Database.getStudent().getCompletedcourses().contains(id)){
                info_list.add("CourseCode: " + courmod.getCoursecode() +"\n" + "Course Completed");
            }
            else{
                info_list.add("CourseCode: " + courmod.getCoursecode());
            }
            if(!String.valueOf(courmod.getCoursecontent()).equals("null")){
                String content = courmod.getCoursecontent();
                String toShow = fitContent(content);
                info_list.add("Content: " + toShow);
                infoTable.setFixedCellSize(0);
            }
            info_list.add("Credits: " + courmod.getCredits().replace("\"", " ").replace("{", "").replace("}", ""));
            if(!courmod.getOutcomes().equals("null")){
                String content = courmod.getOutcomes();
                String toShow = fitContent(content);
                info_list.add("Outcomes: " + toShow);
                infoTable.setFixedCellSize(0);
            }

            name = courmod.getName();
            // Set name to column header.
            infoTable.getColumns().forEach(c -> {
                Label l = new Label(courmod.getName());
                c.setGraphic(l);   
            });
        }
        infoTable.getItems().clear();
        ObservableList<String> details = 
                                   FXCollections.observableArrayList(info_list);
        infoTable.setItems(details);
    
        // Set the tooltip to the header of the infotable as some names 
        // get cut by the TableView.
        Tooltip tooltip = new Tooltip(" "+name);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(220);
        Label label = (Label) infoTable.lookup(".label");
        label.setTooltip(tooltip);
        // Makes the tooltip display, 
        // no matter where the mouse is inside the column header.
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }
     
    /**
     * Create a TreeItem for a TreeView from a set of data
     * given the data and an identified root within the data.
     */
    private TreeItem<String> createTreeItem(TreeMap<String, List<String>> data,
                                                               String rootKey) {
        TreeItem<String> item = new TreeItem<>();
        
        if (rootKey.contains("$")) {
            item.setValue(rootKey.substring(0, rootKey.indexOf("$")));
            
            String id = 
                    rootKey.substring(rootKey.indexOf("$")+1, rootKey.length());
            // Get the type of the item with the id and send it to treeCellIds.
            Module mod = readClassType(id);
            if (mod == null) {
                CourseUnit course = checkIfCourse(id);
                if (course == null) {
                    System.out.print("Sth went wrong with id: "+ id+ "\n");
                }
                else {
                    treeCellIds.put(course.getName(), course.getGroupId());
                }
            }
            else {
                treeCellIds.put(mod.getName(), mod.getGroupId());
            }
        }
        // Special case just in case
        else {
            item.setValue(rootKey);
        }
        item.setExpanded(true);
        
        List<String> childData;
        if (rootKey.contains("$")) {
            childData = data.get(rootKey.substring(0, rootKey.indexOf("$")));

        }
        // Special case just in case
        else {
            childData = data.get(rootKey);
        }
        if (childData != null) {
            childData.stream()
                .sorted()
                .map(
                        child -> createTreeItem(data, child)
                )
                .collect(
                        Collectors.toCollection(item::getChildren)
                );
        }
        return item;
    }
    
    
    @Override
    public void start(Stage stage) {

        importDegrees();
        ObservableList<JSONObject> data = 
              FXCollections.observableArrayList(Database.getDegreeProgrammes());
        
        // Buttons, labels and fields.
        
        Button exitButton = new Button("Exit");
        Button exitButton2 = new Button("Exit");
        Button exitButton3 = new Button("Exit");
        
        Button switchFromNameInputScene = new Button("Proceed");
        Button switchToNameInputScene = new Button("Back");
        
        Button selectDegree = new Button("Select");
        Button backToDegreeSelect = new Button("Back");
        
        Button idModeButton = new Button("idMode");
        idModeButton.setTooltip(new Tooltip("Change names to id's and back in this mode!"));
              
        Button courseDoneButton = new Button("Mark done");

        var nameLabel = new Label("Name: ");
        TextField nameField = new TextField("\"Teemu Teekkari\"");
        var numberLabel = new Label("Student number: ");
        TextField numberField = new TextField("\"A123456\"");
        
        // Labels to show student info
        var stud_info_label = new Label("");
        var stud_info_label2 = new Label("");
        var stud_credits_label = new Label("");
        
        // TableColumns

        TableColumn<JSONObject, String> degreeProgCol = new TableColumn<JSONObject, String>("Choose degree programme:");
        degreeProgCol.setPrefWidth(400);
        degreeTable.setEditable(true);
        degreeTable.setPlaceholder(new Label("No degrees match the search."));
        
        // Callback form enables us to customly call the JSON .get() method.  
        degreeProgCol.setCellValueFactory(new Callback<TableColumn.
              CellDataFeatures<JSONObject, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call
                          (TableColumn.CellDataFeatures<JSONObject, String> p) {
                if (p.getValue() != null) {
                    return new SimpleStringProperty(p.getValue().get("name")
                                                                   .toString());
                } else {
                    return new SimpleStringProperty("<no name>");
                }
            }
        });

        
        //Filtered list to allow searching from the degreeTable. 
        FilteredList<JSONObject> flDegree = new FilteredList<JSONObject>(data, d -> true);
        degreeTable.setItems(flDegree);
        degreeTable.getColumns().add(degreeProgCol);
   
        TextField searchField = new TextField();
        searchField.setPromptText("Search:");
        // Search field updates the showed filtered list based on input.
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
        flDegree.setPredicate(d -> d.get("name").toString().toLowerCase()
                                      .contains(newValue.toLowerCase().trim()));          
        });
        
        // Shows info of selected module or course.
        infoTable.setEditable(true);
        infoTable.setPlaceholder(new Label("Nothing selected"));
        infoTable.setColumnResizePolicy((param)-> true);
        infoTable.setPrefWidth(280);
        infoTable.setFixedCellSize(79.0);
        TableColumn<String, String> col1 = new TableColumn<>();
        Label columnHeader = new Label("Info");
        col1.setGraphic(columnHeader);
        infoTable.getColumns().add(col1);
        col1.setPrefWidth(280);
        col1.setCellValueFactory
                          (data1 -> new SimpleStringProperty(data1.getValue()));
        
        // Grids 
        
        // Initial screen to input student information.
        GridPane nameInputGrid = new GridPane();
        nameInputGrid.add(exitButton, 9,9);
        nameInputGrid.add(nameLabel, 0, 1);
        nameInputGrid.add(nameField, 1, 1);
        nameInputGrid.add(numberLabel, 0, 2);
        nameInputGrid.add(numberField, 1, 2);
        nameInputGrid.add(switchFromNameInputScene, 1, 3);

        nameInputGrid.setPadding(new Insets(10));
        
        // Second screen to choose degree program.
        GridPane degreeInputGrid = new GridPane();
        
        degreeInputGrid.add(exitButton2, 4, 0);
        degreeInputGrid.add(switchToNameInputScene, 0, 0);
        degreeInputGrid.add(stud_info_label, 2, 0);
        GridPane.setHalignment(stud_info_label, HPos.CENTER);
        VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 10, 10));
        vbox.setAlignment(Pos.CENTER_RIGHT);
        vbox.getChildren().addAll(degreeTable, searchField, selectDegree);
        degreeInputGrid.add(vbox, 2, 2);
        
        // Grid for showing a degree programme.
        GridPane degreeShowGrid = new GridPane();
        degreeShowGrid.add(backToDegreeSelect, 1, 0);
        degreeShowGrid.add(exitButton3, 2, 0);
        degreeShowGrid.add(idModeButton, 1, 5);
        GridPane.setHalignment(exitButton3, HPos.RIGHT);
        degreeShowGrid.add(stud_info_label2, 1, 0);
        GridPane.setHalignment(stud_info_label2, HPos.CENTER);
        degreeShowGrid.add(stud_credits_label, 2, 0);
        GridPane.setHalignment(stud_credits_label, HPos.CENTER);
        degreeShowGrid.add(infoTable, 2, 1);
        degreeShowGrid.setPadding(new Insets(10, 0, 10, 10));
        degreeShowGrid.add(courseDoneButton, 2, 0);
        GridPane.setHalignment(courseDoneButton, HPos.LEFT);
        
        // Scenes
        
        Scene nameInputScene = new Scene(nameInputGrid, 405, 155);
        Scene degreeSelectScene = new Scene(degreeInputGrid, 800, 400);
        Scene degreeProgrammeScene = new Scene(degreeShowGrid, 
                                      degreeSelectScene.getWidth(), 
                                      degreeSelectScene.getHeight());
        nameInputScene.getStylesheets().add("SisuSceneStyle.css");
        degreeSelectScene.getStylesheets().add("SisuSceneStyle.css");
        degreeProgrammeScene.getStylesheets().add("SisuSceneStyle.css");
        
        // Button and click actions
        
        /* switchFromNameInputScene only works if Student name consists 
         * of only characters that could be in a name and one space in between.
         */
        BooleanBinding nameScreenBind = new BooleanBinding() {
            {
                super.bind(nameField.textProperty(),numberField.textProperty());
            }
            // Name (and lastname) with only one whitespace in between.
            String nameRx = "^[A-z\\'\\.\\-ᶜ]+(((\\s))[A-z\\'\\.\\-ᶜ]+)*$";
            
            /* We understand this is not the entirely perfect regex for the
             * university student number, but snooping Moodle made us realise
             * there is so much variance we don't want to limit it too much.
             */
            String nrRx = "^[A-z0-9\\-]{5,12}$";
            @Override
            protected boolean computeValue() {
                return (!nameField.getText().matches(nameRx)
                    || !numberField.getText().matches(nrRx));
            }
        };
        switchFromNameInputScene.disableProperty().bind(nameScreenBind);
        
        // Disable proceeding from degree selection if no degree is selected.
        selectDegree.disableProperty().bind(Bindings.isEmpty(degreeTable
                                      .getSelectionModel().getSelectedItems()));
        
        // Might be redundant but keeping it there for now.
        exitButton.setOnAction((event) -> {stage.close();});
        exitButton2.setOnAction((event) -> {stage.close();});
        exitButton3.setOnAction((event) -> {stage.close();});
        
        // Change to the main window to select the degree program.
        switchFromNameInputScene.setOnAction( e -> {
            String stud_name = nameField.getText();
            String stud_number = numberField.getText();
            Student stud = new Student(stud_name, stud_number);
            Database.setStudent(stud);
            stud_info_label.setText(stud_name + "  " + stud_number);
            stud_info_label2.setText(stud_name + "  " + stud_number);
            stud_credits_label.setText("Credits so far: " + 
                                       String.valueOf(stud.getTotal_credits()));
            
            stage.setScene(degreeSelectScene);
            stage.setWidth(800);
            stage.setHeight(400);
            stage.setResizable(true);
        });

        // Return to change student name window.
        switchToNameInputScene.setOnAction((event) -> {
            stage.setScene(nameInputScene);
            stage.setWidth(405);
            stage.setHeight(155);
            stage.setResizable(false);
        });
        
        // Change to showing one degree program and it's contents.
        selectDegree.setOnAction( e -> {
            String groupId = degreeTable.getSelectionModel()
                                   .getSelectedItem().get("groupId").toString();
            // Reduce unnecessary imports.
            if (Database.getDegreeProgrammeMap().get(groupId) == null) {
                importProgram(groupId);
            }

            // Arbitrary additions to account for decorations.
            stage.setWidth(degreeSelectScene.getWidth()+16);
            stage.setHeight(degreeSelectScene.getHeight()+39);

            TreeView<String> tree = createNestedTree(groupId);
            degreeShowGrid.add(tree, 1, 1);
            infoTable.getItems().clear();
            Database.getStudent().getCompletedcourses().clear();
            Database.getStudent().setTotal_credits(0);
            stud_credits_label.setText("Credits so far: " + String.valueOf(Database.getStudent().getTotal_credits()));
            idModeFlag = false;
            stage.setScene(degreeProgrammeScene);
        });
        
         /* 
          *  Doubleclicking a degree will open it like clicking "selectDegree".
          */ 
        degreeTable.setOnMouseClicked(event -> {
            if (event.getClickCount() > 1) {
                String groupId = degreeTable.getSelectionModel()
                        .getSelectedItem().get("groupId").toString();
                // Reduce unnecessary imports.
                if (Database.getDegreeProgrammeMap().get(groupId) == null) {
                    importProgram(groupId);
                }
                
                // Arbitrary additions to account for decorations.
                stage.setWidth(degreeSelectScene.getWidth()+16);
                stage.setHeight(degreeSelectScene.getHeight()+39);
                
                TreeView<String> tree = createNestedTree(groupId);
                degreeShowGrid.add(tree, 1, 1);
                infoTable.getItems().clear();
                stage.setScene(degreeProgrammeScene);
            }
        });
        
        // Back to selecting a programme.
        backToDegreeSelect.setOnAction( e -> {
            // Arbitrary addition accounts for decorations.
            stage.setWidth(degreeProgrammeScene.getWidth()+16);
            stage.setHeight(degreeProgrammeScene.getHeight()+39);
            stage.setScene(degreeSelectScene);
        });
        
        courseDoneButton.setOnAction((event) -> {
            if(Database.getCourseMap().containsKey(selectedCourseId) &&
                    !Database.getStudent().getCompletedcourses().contains(selectedCourseId)){
                Database.getStudent().courseDone(selectedCourseId);
                stud_credits_label.setText("Credits so far: " + String.valueOf(Database.getStudent().getTotal_credits()));
            }
            showInfo(selectedCourseId);
        });


        // Switch id mode on, indicate it with a color change.
        idModeButton.setOnAction( e -> {
            if (idModeFlag) {
                idModeFlag = false;
                idModeButton.setStyle("-fx-background-color: white" );
            }
            else {
                idModeFlag = true;
                idModeButton.setStyle("-fx-background-color: #edc48e" );
            }
        });
        
        // Stage 
        
        stage.setTitle("Sisuttaa");
        stage.setScene(nameInputScene);
        stage.getIcons().add(new Image
                           ("file:src\\main\\resources\\images\\sisuttaa.jpg"));
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        Properties p = System.getProperties();
        p.put("-Dlog4j.configurationFile",
              "file:src\\main\\resources\\log4j2.xml");
        System.setProperties(p);
        
        launch();
    }
}



        


