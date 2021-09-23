package org.cytoscape.myApp.internal;

import org.cytoscape.model.CyNetwork;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
/**
 * NeDRex App
 * @author Judith Bernett
 */
public class QuickSelectPanel2 extends JPanel {
    private RepoApplication app;
    JButton btnSelect;
    JButton btnRemoveLast;
    JButton btnSelectInGraph;
    JButton btnReset;
    private JList<String> jList;
    private JScrollPane jListScrollPane;
    private JTextField tf;
    private Vector<String> v;
    private Map<String, Set<Long>> quickSelectIds;
    private JDialog quickSelectDialog;
    JTextArea quickSelectBox;
    LinkedList<String> selectedNames;
    String selectedType;
    JComboBox<String> nodeTypeBox;
    String[] nodeTypes = {"<Select type>"};

    public QuickSelectPanel2(RepoApplication app) {
        super();
        this.app = app;
        this.jList = new JList<>();
        this.jListScrollPane = new JScrollPane(jList);
        jListScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jListScrollPane.setPreferredSize(new Dimension(70, 100));
        this.tf = new JTextField();
        this.v = new Vector<>();
        this.quickSelectIds = new HashMap<>();
        this.quickSelectBox = new JTextArea();
        this.selectedNames = new LinkedList<>();

        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JLabel chooseLabel = new JLabel();
        chooseLabel.setText("Choose the node type you want to select from:");
        chooseLabel.setHorizontalAlignment(JLabel.LEFT);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        c.weighty = 0.5;
        c.gridwidth = 3;
        btnPanel.add(chooseLabel, c);

        btnSelect = new JButton("Add to the list");
        btnSelect.addActionListener(new AddElementActionListener2(jList, quickSelectBox, selectedNames));

        btnRemoveLast = new JButton("Remove last");
        btnRemoveLast.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(selectedNames.size() != 0) {
                    String element = selectedNames.removeLast();
                    for(Long suid : quickSelectIds.get(element)) {
                        app.getCurrentNetwork().getDefaultNodeTable().getRow(suid).set(CyNetwork.SELECTED, false);
                    }
                    StringJoiner sj = new StringJoiner("\n");
                    for (String d : selectedNames) {
                        sj.add(d);
                    }
                    quickSelectBox.setText(sj.toString()+"\n");
                }
            }
        });

        btnSelectInGraph = new JButton("Select in Network");
        btnSelectInGraph.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                for(String element: selectedNames){
                    for(Long suid : quickSelectIds.get(element)) {
                        app.getCurrentNetwork().getDefaultNodeTable().getRow(suid).set(CyNetwork.SELECTED, true);
                    }
                }

            }
        });

        btnReset = new JButton("Reset Selection");
        btnReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                for(String element: selectedNames){
                    for(Long suid : quickSelectIds.get(element)) {
                        app.getCurrentNetwork().getDefaultNodeTable().getRow(suid).set(CyNetwork.SELECTED, false);
                    }
                }
                selectedNames.clear();
                quickSelectBox.setText("");
            }
        });
        nodeTypeBox = new JComboBox<>(nodeTypes);
        nodeTypeBox.setSelectedIndex(0);
        nodeTypeBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if(!Objects.equals((String) nodeTypeBox.getSelectedItem(), "<Select type>") && nodeTypeBox.getSelectedItem() != null) {
                    selectedType = (String) nodeTypeBox.getSelectedItem();
                    QuickSelectKeyAdapter2 quickSelectKeyAdapter
                            = new QuickSelectKeyAdapter2(selectedType ,tf, jList, v, app, quickSelectIds);
                    tf.addKeyListener(quickSelectKeyAdapter);
                    jList.setEnabled(true);
                    btnSelect.setEnabled(true);
                    btnRemoveLast.setEnabled(true);
                    btnSelectInGraph.setEnabled(true);
                    btnReset.setEnabled(true);
                }
            }
        });
        c.gridy = 1;
        btnPanel.add(nodeTypeBox, c);

        JLabel startLabel = new JLabel();
        startLabel.setText("Type in the disease/drug/gene/protein/pathway name or ID: ");
        startLabel.setHorizontalAlignment(JLabel.LEFT);
        c.gridy = 2;
        btnPanel.add(startLabel, c);

        quickSelectBox.setLineWrap(true);
        quickSelectBox.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(quickSelectBox);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(70, 100));
        quickSelectBox.setEditable(false);
        tf.setEditable(true);
        c.gridy = 3;
        btnPanel.add(tf, c);
        c.gridy = 4;
        btnPanel.add(jListScrollPane, c);
        c.gridy = 5;
        btnPanel.add(btnSelect, c);

        JLabel jLabel = new JLabel();
        jLabel.setText("Added: ");
        jLabel.setHorizontalAlignment(JLabel.LEFT);
        c.gridy = 6;
        btnPanel.add(jLabel, c);
        c.gridy = 7;
        btnPanel.add(scrollPane, c);
        c.gridy = 8;
        c.gridwidth = 1;
        btnPanel.add(btnRemoveLast, c);
        c.gridx = 1;
        btnPanel.add(btnReset, c);
        c.gridx = 2;
        btnPanel.add(btnSelectInGraph, c);
        add(btnPanel);
    }

    public void activate() {
        this.app.getActivator().registerService(this, JPanel.class);
        quickSelectDialog = this.app.getQuickSelectDialog2();
        this.quickSelectDialog.getContentPane().add(this);
        this.quickSelectDialog.pack();
        setVisible(true);
        nodeTypes = initializeComboBox();
        tf.setText("");
        v.clear();
        jList.setModel(new DefaultComboBoxModel<String>(v));
        quickSelectIds.clear();
        selectedNames.clear();
        quickSelectBox.setText("");
        nodeTypeBox.setModel(new DefaultComboBoxModel<>(nodeTypes));
        nodeTypeBox.setSelectedIndex(0);
        jList.setEnabled(false);
        btnSelect.setEnabled(false);
        btnRemoveLast.setEnabled(false);
        btnSelectInGraph.setEnabled(false);
        btnReset.setEnabled(false);
        this.quickSelectDialog.setVisible(true);
    }

    public void deactivate() {
        this.app.getActivator().unregisterAllServices(this);
        quickSelectDialog.setVisible(false);
    }

    private String [] initializeComboBox(){
        Set<String> nodeColumn = new HashSet<>(app.getCurrentNetwork().getDefaultNodeTable().getColumn("type").getValues(String.class));
        List<String> nodeTypesList = new ArrayList<>();
        nodeTypesList.add("<Select type>");
        for(String s: new String[]{"Disorder", "Drug", "Gene", "Protein", "Pathway"}){
            if(nodeColumn.contains(s)){
                nodeTypesList.add(s);
            }
        }
        String[] returnNodeTypes = new String[nodeTypesList.size()];
        returnNodeTypes = nodeTypesList.toArray(returnNodeTypes);
        return returnNodeTypes;
    }

}
