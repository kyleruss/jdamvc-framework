//====================================
//	Kyle Russell
//	StudentCore
//	Window
//====================================

package views.gui.layout;

import jdamvc.engine.view.GUIView;
import java.awt.Point;
import javax.swing.JFrame;
import jdamvc.engine.core.Config;


public class Window extends JFrame
{
    private final Layout layout;
    
    public Window()
    {
        super(Config.APP_NAME);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        layout      =   new Layout();
        getContentPane().add(layout);
        layout.getMenu().attachTo(this);
        
        Point dim   =   getWindowDim();
        setSize(dim.x, dim.y);
        
        setResizable(false);
        setLocationRelativeTo(null);
    }
    
    public void display()
    {
        setVisible(true);
    }
    
    public static Point getWindowDim()
    {
        return new Point(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT);
    }
    
    public Layout getAppLayout()
    {
        return layout;
    }
    
    public void setActiveView(GUIView view)
    {
        layout.getViewPane().setActiveView(view);
    }
}
