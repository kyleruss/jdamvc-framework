//====================================
//	Kyle Russell
//	StudentCore
//	ErrorView
//====================================

package views.gui.layout;

import jdamvc.engine.controller.Message;
import jdamvc.engine.core.Agent;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import jdamvc.engine.view.GUIView;

public class ErrorView extends GUIView
{
    public ErrorView()
    {
        super();
    }
    
    public ErrorView(Message data)
    {
        super(data);
    }
    
    @Override
    protected void initComponents() 
    {
        panel               =   new JPanel();
        panel.setBackground(Color.WHITE);
        JPanel innerPanel   =   new JPanel(new GridLayout(2, 1));
        innerPanel.setBackground(Color.WHITE);
        innerPanel.setBorder(BorderFactory.createEmptyBorder(180, 0, 0, 0));
        
        JLabel errorLabel   =   new JLabel();
        if(!viewData.hasMessages() || viewData.getMessage("errorMessage") == null)
            errorLabel.setText("Error");
        else
            errorLabel.setText(viewData.getMessage("errorMessage").toString());
            
        JLabel redirectMsg  =   new JLabel("You will be redirected back shortly");
        errorLabel.setHorizontalAlignment(JLabel.CENTER);
        redirectMsg.setHorizontalAlignment(JLabel.CENTER);
        
        errorLabel.setFont(new Font("Arial", Font.BOLD, 32));
        redirectMsg.setFont(new Font("Arial", Font.PLAIN, 24));
        
        innerPanel.add(errorLabel);
        innerPanel.add(redirectMsg);
        panel.add(innerPanel);

        Timer timer =   new Timer(5000, (ActionEvent e)->
        {
            Agent.setPrevView();
        });
        
        timer.setRepeats(false);
        timer.start();
    }
    
    @Override
    protected void initResources()  {}

    @Override
    protected void initListeners() {}
}
