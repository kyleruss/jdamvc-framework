//====================================
//	Kyle Russell
//	jdamvc
//	Agent
//====================================

package jdamvc.engine.core;

import jdamvc.engine.controller.Message;
import jdamvc.engine.core.authentication.Session;
import jdamvc.engine.core.authentication.StoredCredentials;
import jdamvc.engine.view.GUIView;
import jdamvc.engine.view.View;
import jdamvc.engine.view.layout.Window;
import java.awt.event.ActionEvent;
import java.util.Scanner;
import javax.swing.Timer;
import jdamvc.engine.view.layout.Layout;


//------------------------------------------
//              AGENT
//------------------------------------------
//- Agent is the main class and controls the running of the app
//- Is the bridge of communication between sessions, views and data

public final class Agent extends CommandInterpreter
{    
    //A conext is the flag for which listener handles communication
    //Contexts include agent and view (active)
    public enum Context
    {
        AGENT("agent"),
        VIEW("current");
        
        //The command trigger/prefix
        private final String trigger;
        
        Context(String trigger)
        {
            this.trigger    =   trigger;
        }
        
        //Returns the context trigger/prefix
        public String getTrigger()
        {
            return trigger;
        }
        
        //Returns the context based on trigger 
        public static Context getContext(String trigger)
        {
            return Context.valueOf(trigger);
        }
    }
    
    private static View activeView; //The current view being displayed
    private static Context activeContext; //The current context of input
    private static Session activeSession; //The session, includes auth and logged in users
    private static Thread agentThread;  //Thread for agent command input and tasks
    private static StoredCredentials storedCredentials;
    private static volatile boolean waitingOnCommand = false; //false when view is controlling input
    private static volatile boolean serving = true; //The agents life flag, true if agent is working
    private static Window window;
    private static boolean guiMode;
    
    //Creates and starts the apps agent
    public Agent()
    {
        guiMode             =   Config.GUI_MODE;
        agentThread         =   new Thread(listen);
        fetchCredentials();
        begin();
    }
    
    //Starts the agent
    //Loads the initial view at route address '/'
    //Context is handed over to starting view
    private void begin()
    {
        final String startRoute   =   "/";
        
        if(guiMode)
        {
            window  =   Window.getWindowInstance();
            window.display();
        }
        
        setViewContext();
        setView(startRoute);
        agentThread.start();        
    }
    
    //Call when view controlling input is finished
    //Control will be handed back to Agent
    public static void commandFinished()
    {
        synchronized(agentThread)
        {
            waitingOnCommand = false;
            agentThread.notify();
        }
    }
    
    //Give control of input to current view
    //When finished call commandFinished()
    public static void commandInProgress()
    {
        synchronized(agentThread)
        {
            waitingOnCommand = true;
            agentThread.notify();
        }
    }
    
    //End the agent session
    public static void stopServing()
    {
        synchronized(agentThread)
        {
            serving =   false;
            agentThread.notify();
        }
    }
    
    public static void fetchCredentials()
    {
        storedCredentials   =   StoredCredentials.getSavedCredentials();
        
        if(storedCredentials == null)
            storedCredentials = new StoredCredentials();
    }
    
    public static StoredCredentials getStoredCredentials()
    {
        return storedCredentials;
    }
    
    //Set the current context to AGENT
    public void setAgentContext()
    {
        activeContext   =   Context.AGENT;
    }
    
    //Set the current context to VIEW
    public void setViewContext()
    {
        activeContext   =   Context.VIEW;
    }
    
    //Set the Agents auth session to activeSession
    public static void setActiveSession(Session activeSession)
    {
        Agent.activeSession   =   activeSession;
    }
    
    //Returns the agents active session
    //activeSession is null if no auth user
    public static Session getActiveSession()
    {
        return activeSession;
    }
    
    //Set and display the passed view
    //Handles ViewExplorer pointers for navigation
    public static void setView(View view)
    {
        if(view == null)
        {
            if(!guiMode)
                ExceptionOutput.output("View not found", ExceptionOutput.OutputType.MESSAGE);
            else
            {
                View errorView = RouteHandler.go(Config.ERROR_VIEW_ROUTE, new Object[] { "View not found" }, new Class<?>[] { String.class }, null);
                setView(errorView);
            }
        }

        else
        {
            if(activeView != null && view != activeView)
            {
                //prevent creating paradox
                if(view.getNextView() != activeView)
                    view.setPrevView(activeView);

                if(activeView.getPrevView() != view)
                    activeView.setNextView(view);
            }

            activeView =   view;

            if(guiMode)
            {
                Layout layout    =   window.getAppLayout();                

                layout.getViewPane().showTransition();
                Timer transitionTimer    =   new Timer(1500, (ActionEvent e) -> 
                {
                    window.setActiveView((GUIView) activeView);
                    layout.getViewPane().hideTransition();
                });

                transitionTimer.setRepeats(false);
                transitionTimer.start(); 
            }

            else view.display(); 
        }
    }
    
    //Sets the view to the view with name route
    public static void setView(String route)
    {
        setView(route, null);
    }
    
    //Changes the view to route with params data
    public static void setView(String route, Message data)
    {
        View controllerView =   RouteHandler.go(route, data);
        setView(controllerView);
    }
    
    //Sets the current view to the previous view
    //The ViewExplorer must be atleast 2 pages deep to go back
    public static void setPrevView()
    {
        if(activeView == null || activeView.getPrevView() == null)
            ExceptionOutput.output("Can't go to that view", ExceptionOutput.OutputType.DEBUG);
        else
            setView(activeView.getPrevView());
        
    }
    
    //Sets the current view to the next view
    //Next view exists if user goes back
    //then wants to traverse the explorer forward
    public static void setNextView()
    {
        if(activeView == null || activeView.getNextView() == null)
            ExceptionOutput.output("Can't go to that view", ExceptionOutput.OutputType.DEBUG);
        else
            setView(activeView.getNextView());
    }
    
    //Refreshes the current view by re-displaying the view
    public static void refreshView()
    {
         if(activeView == null)
             ExceptionOutput.output("Can't refresh this view", ExceptionOutput.OutputType.DEBUG);
         else
         {
             if(!guiMode) activeView.display();
             else setView(activeView);
         }
    }

    public static Window getWindow()
    {
        return window;
    }
    
    //Terminates the Agent session
    //If no other work, app is killed
    public void exitApp()
    {
        stopServing();
    }
    
    //Returns true if the agent mode is GUI
    //Controllers and other need to know this for view return
    public static boolean isGUIMode()
    {
        return guiMode;
    }
    
    //Logs the current user out if they are logged in
    //User is redirected after 5 seconds 
    public static void sessionLogout()
    {      
        if(!guiMode)
        {
            
            if(activeSession == null)
            {
                ExceptionOutput.output("You aren't logged in!", ExceptionOutput.OutputType.DEBUG);
                return;
            }
            
            ExceptionOutput.output("You have been logged out\n", ExceptionOutput.OutputType.DEBUG);
            System.out.println("Redirecting in 5 seconds..");
        
      
            try { Thread.sleep(5000); } 
            catch (InterruptedException ex) 
            {
                ExceptionOutput.output("[Interrupted in logout] " + ex.getMessage(), ExceptionOutput.OutputType.DEBUG);
            }
        }
        
        setView("logout");
    }
    
    //Sets the current context of agent
    public void switchContext(Context context)
    {
        activeContext  =   context;
    }

    //Checks if the command is an agent context command or if activeContext
    //Agent commands are prefixed by the Context.Agent.Trigger() 
    //These commands pierce the views command handler and go straight to agent
    public boolean isAgentContext(String command)
    {
        if(command == null || command.length() == 0 || activeContext == Context.AGENT) return true;
        
        //Check prefixed trigger in command
        String[] params =   command.split(" ");
        String trigger  =   Context.AGENT.getTrigger();
        return (params[0].equalsIgnoreCase(trigger + ":")) || commands.containsKey(params[0]);
    } 
    
    //Returns the agents listener
    @Override
    public String getListenerPath()
    {
        return "/engine/config/listeners/AgentListener.json";
    }
    
    //The agents listening worker
    //Takes and interpretes commands passed to agent based on context
    //Agent handling of commands can be swapped to view by commandInProgress()
    //Agent runs until it is finished servering - stopServing() or exitApp()
    Runnable listen =   () -> 
    {
        Scanner input           =   new Scanner(System.in);
        String command;
        
        synchronized(agentThread)
        {     
            try
            {
                while(serving)
                {
                    //Command may be handled by current view
                    //Control is returned when view is done
                    while(waitingOnCommand)
                        agentThread.wait();
                
                    command = input.nextLine();
                    
                    //Let agent handle command interpretation
                    if(isAgentContext(command))
                    {
                        String commandStr   =   command.replace("agent: ", "");
                        fire(commandStr, Agent.this);
                    }

                    //Command sent to current view
                    else if(activeView != null)
                        activeView.fire(command, activeView);    
                }
            }
            
            catch(InterruptedException e)
            {
                ExceptionOutput.output("[Agent listener interrupted] " + e.getMessage(), ExceptionOutput.OutputType.DEBUG);
            }     
        }
    };
   
    
    public static void main(String[] args)
    {
        Agent agent =   new Agent();
    }
}