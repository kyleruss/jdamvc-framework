//====================================
//	Kyle Russell
//	jdamvc
//	BaseController
//====================================

package controllers;

import views.gui.layout.ErrorView;
import views.gui.layout.TransitionView;
import jdamvc.engine.controller.Controller;
import jdamvc.engine.controller.Message;
import jdamvc.engine.view.View;

public class MasterController extends Controller 
{       
    public View getTransitionView()
    {
        return new TransitionView();
    }
    
    //Returns an error view
    //The passed errorMessage is displayed in the views title
    //GUI Only - CUI should report errors normally
    public View getErrorView(String errorMessage)
    {
        Message data  =   new Message();
        data.add("errorMessage", errorMessage);
        return new ErrorView(data);
    }
}
