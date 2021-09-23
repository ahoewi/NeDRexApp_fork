package org.cytoscape.myApp.internal;

import org.cytoscape.model.CyNode;
import org.cytoscape.myApp.internal.utils.FilterType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;
/**
 * NeDRex App
 * @author Judith Bernett
 */
public class QuickSelectKeyAdapter2  extends KeyAdapter {
    private final JTextField tf;
    private final JList<String> jList;
    private boolean hide_flag = false;
    private final Vector<String> v;
    private Map<String, Set<Long>> quickSelectIds;
    private RepoApplication app;

    public QuickSelectKeyAdapter2(String selectedType, JTextField tf, JList<String> jList, Vector<String> v, RepoApplication app, Map<String, Set<Long>> quickSelectIds) {
        this.tf = tf;
        this.jList = jList;
        this.v = v;
        this.v.clear();
        this.app = app;
        this.quickSelectIds = quickSelectIds;

        switch (selectedType) {
            case "Disorder":
                putElements(NodeType.Disease);
                break;
            case "Drug":
                putElements(NodeType.Drug);
                break;
            case "Gene":
                putElements(NodeType.Gene);
                break;
            case "Protein":
                putProteins();
                break;
            case "Pathway":
                putElements(NodeType.Pathway);
        }
    }

    private void putProteins(){
        Set<String> allElements = Collections.synchronizedSet(new HashSet<>());
        try{
            Set<CyNode> allNodes = FilterType.nodesOfType(app.getCurrentNetwork(), NodeType.Protein);
            allNodes.forEach(node -> putElementsFromNode(node, allElements, true));
        }catch (Exception e){
            allElements.add("Failed");
        }
        addElements(allElements);
    }

    private void putElements(NodeType type){
        Set<String> allElements = new HashSet<>();
        try{
            Set<CyNode> allNodes = FilterType.nodesOfType(app.getCurrentNetwork(), type);
            allNodes.forEach(node -> putElementsFromNode(node, allElements, false));
        }catch (Exception e){
            allElements.add("Failed");
        }
        addElements(allElements);
    }

    private void putElementsFromNode(CyNode node, Set<String> allElements, boolean protein){
        Long suid = node.getSUID();
        String sharedName = app.getCurrentNetwork().getDefaultNodeTable().getRow(suid).get("shared name", String.class);
        checkMap(allElements, suid, sharedName);

        if(protein) {
            String geneName = app.getCurrentNetwork().getDefaultNodeTable().getRow(suid).get("geneName", String.class);
            if (geneName != null) {
                geneName = geneName + " (Gene Name)";
                checkMap(allElements, suid, geneName);

            }
        }else{
            String displayName = app.getCurrentNetwork().getDefaultNodeTable().getRow(suid).get("displayName", String.class);
            checkMap(allElements, suid, displayName);
        }
    }

    private void checkMap(Set<String> allElements, Long suid, String attribute) {
        if (quickSelectIds.get(attribute) == null) {
            HashSet<Long> tmp = new HashSet<>();
            tmp.add(suid);
            quickSelectIds.put(attribute, tmp);
        } else {
            quickSelectIds.get(attribute).add(suid);
        }

        allElements.add(attribute);
    }


    private void addElements(Set<String> allElements){
        List<String> listElements = new ArrayList<>(allElements);
        allElements.clear();
        Collections.sort(listElements);
        for (String element : listElements) {
            v.addElement(element);
        }
        setModel(new DefaultComboBoxModel<String>(v), "");
    }

    public void keyTyped(KeyEvent e) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                String text = tf.getText();
                if (text.length() == 0) {
                    jList.setVisibleRowCount(0);
                    setModel(new DefaultComboBoxModel<String>(v), "");
                }else {
                    DefaultComboBoxModel<String> m = getSuggestedModel(v, text);
                    if (m.getSize() == 0 || hide_flag) {
                        jList.setVisibleRowCount(0);
                        hide_flag = false;
                    } else {
                        setModel(m, text);
                        jList.setVisibleRowCount(-1);
                    }
                }
            }
        });
    }


    private void setModel(DefaultComboBoxModel<String> mdl, String str) {
        jList.setModel(mdl);
        jList.setSelectedIndex(-1);
        tf.setText(str);
    }

    private DefaultComboBoxModel<String> getSuggestedModel(java.util.List<String> list, String text) {
        DefaultComboBoxModel<String> m = new DefaultComboBoxModel<String>();
        for (String s : list) {
            if (s.toLowerCase().contains(text.toLowerCase()))
                m.addElement(s);
        }
        return m;
    }

}
