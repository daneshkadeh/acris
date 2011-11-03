package sk.seges.test.various.client;

import java.util.ArrayList;
import java.util.List;

import com.allen_sauer.gwt.dnd.client.PickupDragController;
import com.allen_sauer.gwt.dnd.client.drop.SimpleDropController;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.PlaceTokenizer;
import com.google.gwt.place.shared.Prefix;
import com.google.gwt.place.shared.WithTokenizers;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class VariousHacks2 implements EntryPoint {
	private EventBus bus = new SimpleEventBus();
	private PlaceController placeController = new PlaceController(bus);
	private MouseDownHandler mouseDown;
	private PickupDragController ctrl;
	
	@WithTokenizers({FirstTokenizer.class})
	public interface MyPlaceMapper extends PlaceHistoryMapper {
	}

	@Prefix("hehe")
	public static class FirstTokenizer implements PlaceTokenizer<Place> {

		@Override
		public Place getPlace(String token) {
			return new Place() {
			};
		}

		@Override
		public String getToken(Place place) {
			return "tkn";
		}
	}
	
//	@Prefix("huhu")
//	public interface SecondTokenizer extends PlaceTokenizer<Place> {
//	}
	public void onModuleLoad() {
	    // set uncaught exception handler
	    GWT.setUncaughtExceptionHandler(new GWT.UncaughtExceptionHandler() {
	      public void onUncaughtException(Throwable throwable) {
	        String text = "Uncaught exception: ";
	        while (throwable != null) {
	          StackTraceElement[] stackTraceElements = throwable.getStackTrace();
	          text += throwable.toString() + "\n";
	          for (int i = 0; i < stackTraceElements.length; i++) {
	            text += "    at " + stackTraceElements[i] + "\n";
	          }
	          throwable = throwable.getCause();
	          if (throwable != null) {
	            text += "Caused by: ";
	          }
	        }
	        DialogBox dialogBox = new DialogBox(true, false);
	        DOM.setStyleAttribute(dialogBox.getElement(), "backgroundColor", "#ABCDEF");
	        System.err.print(text);
	        text = text.replaceAll(" ", "&nbsp;");
	        dialogBox.setHTML("<pre>" + text + "</pre>");
	        dialogBox.center();
	      }
	    });

	    // use a deferred command so that the handler catches onModuleLoad2() exceptions
	    DeferredCommand.addCommand(new Command() {
	      public void execute() {
	        onModuleLoad2();
	      }
	    });
	  }

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad2() {
		mouseDown = new MouseDownHandler() {
			
			@Override
			public void onMouseDown(MouseDownEvent event) {
				Widget source = (Widget) event.getSource();
				ctrl.makeDraggable(source);
			}
		};
		
		ctrl = new PickupDragController(RootPanel.get("root"), true);
		
		PlaceHistoryMapper mapper = GWT.create(MyPlaceMapper.class);
		PlaceHistoryHandler handler = new PlaceHistoryHandler(mapper);
		handler.register(placeController, bus, Place.NOWHERE);
		
		Element inner = getSourceHTML();
		ElementFlowPanel root = new ElementFlowPanel(RootPanel.get("root").getElement());
		final ProcessingContext context = new ProcessingContext();
		context.root = root;
		context.parent = root;
		parseTemplate(inner, context);
		
		bus.addHandler(PlaceChangeEvent.TYPE, new PlaceChangeEvent.Handler() {
			@Override
			public void onPlaceChange(PlaceChangeEvent event) {
				GWT.log("changed " + event.toString());
				RootPanel.get("root").getElement().setInnerHTML("<div class=\"place-center\"><div class=\"textBlock\">hehe textik</div></div>");
				Element inner = getSourceHTML();
				context.places.clear();
				context.root.clear();
				parseTemplate(inner, context);
			}
		});
	}
	
	private void parseTemplate(Element root, ProcessingContext context) {
		process(context, root);
	}
	
	
	private void process(ProcessingContext context, Element root) {
		Element child = root.getFirstChildElement();
		while(child != null) {
			String className = child.getClassName();
			ElementFlowPanel wrapped = null;
			if(className != null && className.startsWith("place-")) {
				wrapped = wrap(context, child, className);
			}
			if(className != null && className.startsWith("cmp-")) {
				wrapped = wrapComponent(context, child, className);
			}
			if(className != null && className.endsWith("Block")) {
				wrapped = wrapBlock(context, child, className);
			}
			
			if(child.hasChildNodes()) {
				if(wrapped != null) {
					context.parent = wrapped;
				}
				process(context, child);
			}
			
			child = child.getNextSiblingElement();
		}
	}
	
	private ElementFlowPanel wrapBlock(ProcessingContext context, Element child, String className) {
		ElementFlowPanel panel = new ElementFlowPanel(child);
		panel.addStyleName("wrapped-block");

		ctrl.makeDraggable(panel);
//		panel.addMouseDownHandler(mouseDown);
		
//		context.components.add(panel);
		
		if(context.parent != null) {
			context.parent.add(panel);
		}
		
		
		return panel;
	}
	
	private ElementFlowPanel wrapComponent(ProcessingContext context, Element child, String className) {
		ElementFlowPanel panel = new ElementFlowPanel(child);
		panel.addStyleName("wrapped-component");
		
		context.components.add(panel);
		
		if(context.parent != null) {
			context.parent.add(panel);
		}
		
		
		
		return panel;
	}
	
	private ElementFlowPanel wrap(ProcessingContext context, Element child, String className) {
		ElementFlowPanel panel = new ElementFlowPanel(child);
		panel.addStyleName("wrapped-place");
		
		context.places.add(panel);
		SimpleDropController drop = new SimpleDropController(panel);
		ctrl.registerDropController(drop);
		
		if(context.parent != null) {
			context.parent.add(panel);
		}
		
		return panel;
	}
	
	private Element getSourceHTML() {
		return RootPanel.get("root").getElement();
	}
	
	class ElementFlowPanel extends FlowPanel implements HasClickHandlers, HasAllMouseHandlers {

		protected Element element;

		public ElementFlowPanel() {
			this(DOM.createDiv());
		}

		public ElementFlowPanel(Element element) {
			this.element = element;
		}

		public com.google.gwt.user.client.Element getElement() {
			return (com.google.gwt.user.client.Element) this.element;
		}
		
		public void setThisElement(Element element) {
			this.element = element;
		}
		
		  public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
			    return addDomHandler(handler, MouseDownEvent.getType());
			  }
		  public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
			    return addDomHandler(handler, MouseMoveEvent.getType());
			  }

			  public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
			    return addDomHandler(handler, MouseOutEvent.getType());
			  }

			  public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
			    return addDomHandler(handler, MouseOverEvent.getType());
			  }

			  public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
			    return addDomHandler(handler, MouseUpEvent.getType());
			  }
			  public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {
				    return addDomHandler(handler, MouseWheelEvent.getType());
				  }


	    @Override
	    public HandlerRegistration addClickHandler(ClickHandler handler) {
	        return addDomHandler(handler, ClickEvent.getType());
	    }
	    
	    protected void add(Widget child, Element container) {
	        // Detach new child.
	        child.removeFromParent();

	        // Logical attach.
	        getChildren().add(child);

	       
	        // Adopt.
	        adopt(child);
	    }
	}
	
	private class ProcessingContext {
		public List<Widget> places = new ArrayList<Widget>();
		public List<Widget> components = new ArrayList<Widget>();
		public FlowPanel root;
		public Panel parent;
	}
}