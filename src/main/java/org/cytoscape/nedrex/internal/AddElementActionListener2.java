package org.cytoscape.nedrex.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class AddElementActionListener2 implements ActionListener {

    JList<String> jList;
    JTextArea quickSelectBox;
    List<String> selectedNames;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public AddElementActionListener2(JList<String> jList, JTextArea quickSelectBox, List<String> selectedNames){
        this.jList = jList;
        this.quickSelectBox = quickSelectBox;
        this.selectedNames = selectedNames;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        List<String> diseases = jList.getSelectedValuesList();
        for(String disease: diseases) {
            logger.info(disease);
            selectedNames.add(disease);
            quickSelectBox.append(disease + "\n");
        }
    }
}
