//====================================
//	Kyle Russell
//	StudentCore
//	Window
//====================================

package jdamvc.engine.view.layout;

import java.awt.MenuBar;
import jdamvc.engine.view.GUIView;
import java.awt.Point;
import javax.swing.JFrame;
import jdamvc.engine.core.Config;


public class Window extends JFrame
{
    private final Layout layout;
    private static Window window;
    
    private Window()
    {
        super(Config.APP_NAME);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        layout      =   new Layout();
        getContentPane().add(layout);
        
        Point dim   =   getWindowDim();
        setSize(dim.x, dim.y);
        
        setResizable(Config.WINDOW_RESIZE);
        
        if(Config.WINDOW_CENTRE)
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
    
    public static void attachMenu(MenuBar menu)
    {
        window.setMenuBar(menu);
    }
    
    public static Window getWindowInstance()
    {
        if(window == null)
            window = new Window();
        
        return window;
    }
}
