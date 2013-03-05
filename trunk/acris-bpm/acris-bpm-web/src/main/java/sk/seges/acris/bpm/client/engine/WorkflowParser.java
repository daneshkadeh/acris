/**
 * 
 */
package sk.seges.acris.bpm.client.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.bpmn.Condition;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;
import org.activiti.engine.impl.pvm.PvmProcessDefinition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.ScopeImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;

import sk.seges.acris.bpm.client.engine.activity.ClientTaskDelegateExpressionActivityBehavior;
import sk.seges.acris.bpm.client.engine.activity.bpmn.BpmnConstants;
import sk.seges.acris.bpm.client.engine.activity.bpmn.ExclusiveGatewayActivity;
import sk.seges.acris.bpm.client.engine.activity.bpmn.NoneEndEventActivity;
import sk.seges.acris.bpm.client.engine.activity.bpmn.NoneStartEventActivity;

import com.google.gwt.core.client.GWT;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

/**
 * @author ladislav.gazo
 */
public class WorkflowParser {
	private final BeanProvider provider;
	/**
	 * A map for storing sequence flow based on their id during parsing.
	 */
	protected Map<String, TransitionImpl> sequenceFlows = new HashMap<String, TransitionImpl>();
	
	public WorkflowParser(BeanProvider provider) {
		super();
		this.provider = provider;
	}

	public PvmProcessDefinition parse(String bpmnDefinition) {
		Document document = XMLParser.parse(bpmnDefinition);

		NodeList processesList = document.getElementsByTagName("process");
		Node process = processesList.item(0);
		long start = System.currentTimeMillis();
		PvmProcessDefinition parseProcess = parseProcess(process);
		start = System.currentTimeMillis() - start;
		GWT.log("time = " + start);
		return parseProcess;
	}

	private PvmProcessDefinition parseProcess(Node process) {
		ProcessDefinitionImpl processDefinition = new ProcessDefinitionImpl(process.getAttributes()
				.getNamedItem("id").getNodeValue());

		/*
		 * Mapping object model - bpmn xml: processDefinition.id -> generated by
		 * activiti engine processDefinition.key -> bpmn id (required)
		 * processDefinition.name -> bpmn name (optional)
		 */
		processDefinition.setName(process.getAttributes().getNamedItem("name").getNodeValue());

		parseScope(process, processDefinition);

		return processDefinition;
	}

	public void parseScope(Node scopeElement, ScopeImpl parentScope) {

		// Not yet supported on process level (PVM additions needed):
		// parseProperties(processElement);

		NodeList childNodes = scopeElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if ("startEvent".equals(item.getNodeName())) {
				parseStartEvents(item, parentScope);
			} else if ("endEvent".equals(item.getNodeName())) {
				parseEndEvents(item, parentScope);
			} else {
				parseActivities(item, parentScope);
			}
		}

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node item = childNodes.item(i);
			if ("sequenceFlow".equals(item.getNodeName())) {
				parseSequenceFlow(item, parentScope);
			}
		}
		
		// parseBoundaryEvents(scopeElement, parentScope);
		
		// parseExecutionListenersOnScope(scopeElement, parentScope);

		// IOSpecification ioSpecification =
		// parseIOSpecification(scopeElement.element("ioSpecification"));
		// parentScope.setIoSpecification(ioSpecification);
	}

	public void parseStartEvents(Node startEventElement, ScopeImpl scope) {

		ActivityImpl startEventActivity = parseAndCreateActivityOnScopeElement(startEventElement, scope);

		if (scope instanceof ProcessDefinitionImpl) {
			ProcessDefinitionImpl processDefinition = (ProcessDefinitionImpl) scope;
			if (processDefinition.getInitial() != null) {
				// in order to support this, the initial should here be replaced
				// with
				// a kind of hidden decision activity that has pvm transitions
				// to all
				// of the visible bpmn start events
				throw new ActivitiException(
						"multiple startEvents in a process definition are not yet supported"
								+ startEventElement);
			}
			processDefinition.setInitial(startEventActivity);

			// StartFormHandler startFormHandler;
			// String startFormHandlerClassName =
			// startEventElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS,
			// "formHandlerClass");
			// if (startFormHandlerClassName!=null) {
			// startFormHandler = (StartFormHandler)
			// ReflectUtil.instantiate(startFormHandlerClassName);
			// } else {
			// startFormHandler = new DefaultStartFormHandler();
			// }
			// startFormHandler.parseConfiguration(startEventElement,
			// deployment, processDefinition, this);
			//
			// processDefinition.setStartFormHandler(startFormHandler);

		} else {
			scope.setProperty(BpmnConstants.PROPERTYNAME_INITIAL, startEventActivity);
		}

		// Currently only none start events supported

		// TODO: a subprocess is only allowed to have a none start event
		startEventActivity.setActivityBehavior(new NoneStartEventActivity());

	}

	public ActivityImpl parseAndCreateActivityOnScopeElement(Node activityElement, ScopeImpl scopeElement) {
		String id = activityElement.getAttributes().getNamedItem("id").getNodeValue();
		String name = activityElement.getAttributes().getNamedItem("name").getNodeValue();
		ActivityImpl activity = scopeElement.createActivity(id);
		activity.setProperty("name", name);
		activity.setProperty("type", activityElement.getNodeName());
		return activity;
	}

	public void parseEndEvents(Node endEventElement, ScopeImpl scope) {
		ActivityImpl activity = parseAndCreateActivityOnScopeElement(endEventElement, scope);

		// Only none end events are currently supported
		activity.setActivityBehavior(new NoneEndEventActivity());

	}

	public void parseActivities(Node activityElement, ScopeImpl scopeElement) {
		String nodeName = activityElement.getNodeName();
		if (nodeName.equals("exclusiveGateway")) {
			parseExclusiveGateway(activityElement, scopeElement);
			// } else if
			// (activityElement.getTagName().equals("parallelGateway")) {
			// parseParallelGateway(activityElement, scopeElement);
			// } else if (activityElement.getTagName().equals("scriptTask")) {
			// parseScriptTask(activityElement, scopeElement);
		} else if (nodeName.equals("serviceTask")) {
			parseServiceTask(activityElement, scopeElement);
			// } else if (activityElement.getNodeName().equals("task")) {
			// parseTask(activityElement, scopeElement);
			// } else if (activityElement.getTagName().equals("manualTask")) {
			// parseManualTask(activityElement, scopeElement);
			// } else if (activityElement.getNodeName().equals("userTask")) {
			// parseUserTask(activityElement, scopeElement);
			// } else if (activityElement.getTagName().equals("sendTask")) {
			// parseSendTask(activityElement, scopeElement);
			// } else if (activityElement.getNodeName().equals("receiveTask")) {
			// parseReceiveTask(activityElement, scopeElement);
			// } else if (activityElement.getTagName().equals("subProcess")) {
			// parseSubProcess(activityElement, scopeElement);
			// } else if (activityElement.getTagName().equals("callActivity")) {
			// parseCallActivity(activityElement, scopeElement);
			// } else if (activityElement.getTagName().equals("adHocSubProcess")
			// || activityElement.getTagName().equals("businessRuleTask")
			// || activityElement.getTagName().equals("complexGateway")
			// || activityElement.getTagName().equals("eventBasedGateway")
			// || activityElement.getTagName().equals("transaction")) {
			// addWarning("Ignoring unsupported activity type",
			// activityElement);
		}
	}

	public void parseExclusiveGateway(Node exclusiveGwElement, ScopeImpl scope) {
		ActivityImpl activity = parseAndCreateActivityOnScopeElement(exclusiveGwElement, scope);
		activity.setActivityBehavior(new ExclusiveGatewayActivity());

		// parseExecutionListenersOnScope(exclusiveGwElement, activity);
	}

	protected String getAttributeValue(Node node, String attribute) {
		Node attributeNode = node.getAttributes().getNamedItem(attribute);
		if(attributeNode == null) {
			return null;
		}
		return attributeNode.getNodeValue();
	}
	
	public void parseServiceTask(Node serviceTaskElement, ScopeImpl scope) {
		ActivityImpl activity = parseAndCreateActivityOnScopeElement(serviceTaskElement, scope);

//		String type = serviceTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "type");
		String type = null;
		
//		String className = serviceTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS, "class");
		String className = getAttributeValue(serviceTaskElement, BpmnConstants.ACTIVITI_PREFIX + "class");
		
//		String expression = serviceTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS,
//				"expression");
		String expression = getAttributeValue(serviceTaskElement, BpmnConstants.ACTIVITI_PREFIX + "expression");
		
//		String delegateExpression = serviceTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS,
//				"delegateExpression");
		String delegateExpression = getAttributeValue(serviceTaskElement, BpmnConstants.ACTIVITI_PREFIX + "delegateExpression");
		
//		String resultVariableName = serviceTaskElement.attributeNS(BpmnParser.ACTIVITI_BPMN_EXTENSIONS_NS,
//				"resultVariableName");
//		String implementation = serviceTaskElement.attribute("implementation");
//		String operationRef = this.resolveName(serviceTaskElement.attribute("operationRef"));

		if (type != null) {
//			if (type.equalsIgnoreCase("mail")) {
//				parseEmailServiceTask(activity, serviceTaskElement,
//						parseFieldDeclarations(serviceTaskElement));
//			} else {
//				throw new ActivitiException("Invalid usage of type attribute: '" + type + "'" + serviceTaskElement);
//			}
//
//		} else if (className != null && className.trim().length() > 0) {
//			if (resultVariableName != null) {
//				addError("'resultVariableName' not supported for service tasks using 'class'",
//						serviceTaskElement);
//			}
//			activity.setActivityBehavior(new ClassDelegate(className,
//					parseFieldDeclarations(serviceTaskElement)));
//
		} else if (delegateExpression != null) {
//			if (resultVariableName != null) {
//				addError("'resultVariableName' not supported for service tasks using 'delegateExpression'",
//						serviceTaskElement);
//			}
			activity.setActivityBehavior(new ClientTaskDelegateExpressionActivityBehavior(provider, delegateExpression, parseFieldDeclarations(serviceTaskElement)));
		}else if (expression != null) {
//			if (resultVariableName != null) {
//			addError("'resultVariableName' not supported for service tasks using 'delegateExpression'",
//					serviceTaskElement);
//		}
		activity.setActivityBehavior(new ClientTaskDelegateExpressionActivityBehavior(provider, expression, parseFieldDeclarations(serviceTaskElement)));
//		} else if (expression != null && expression.trim().length() > 0) {
//			activity.setActivityBehavior(new ServiceTaskExpressionActivityBehavior(expressionManager
//					.createExpression(expression), resultVariableName));
//
//		} else if (implementation != null && operationRef != null
//				&& implementation.equalsIgnoreCase("##WebService")) {
//			if (!this.operations.containsKey(operationRef)) {
//				addError(operationRef + " does not exist", serviceTaskElement);
//			} else {
//				Operation operation = this.operations.get(operationRef);
//				WebServiceActivityBehavior webServiceActivityBehavior = new WebServiceActivityBehavior(
//						operation);
//
//				Element ioSpecificationElement = serviceTaskElement.element("ioSpecification");
//				if (ioSpecificationElement != null) {
//					IOSpecification ioSpecification = this.parseIOSpecification(ioSpecificationElement);
//					webServiceActivityBehavior.setIoSpecification(ioSpecification);
//				}
//
//				for (Element dataAssociationElement : serviceTaskElement.elements("dataInputAssociation")) {
//					AbstractDataInputAssociation dataAssociation = this
//							.parseDataInputAssociation(dataAssociationElement);
//					webServiceActivityBehavior.addDataInputAssociation(dataAssociation);
//				}
//
//				for (Element dataAssociationElement : serviceTaskElement.elements("dataOutputAssociation")) {
//					AbstractDataOutputAssociation dataAssociation = this
//							.parseDataOutputAssociation(dataAssociationElement);
//					webServiceActivityBehavior.addDataOutputAssociation(dataAssociation);
//				}
//
//				activity.setActivityBehavior(webServiceActivityBehavior);
//			}
//		} else {
//			addError(
//					"One of the attributes 'class', 'delegateExpression', 'type', 'operation', or 'expression' is mandatory on serviceTask.",
//					serviceTaskElement);
		}

//		parseExecutionListenersOnScope(serviceTaskElement, activity);

	}

	public void parseSequenceFlow(Node sequenceFlowElement, ScopeImpl scope) {

		String id = getAttributeValue(sequenceFlowElement, "id");
		String sourceRef = getAttributeValue(sequenceFlowElement, "sourceRef");
		
		String destinationRef = getAttributeValue(sequenceFlowElement, "targetRef");

		// Implicit check: sequence flow cannot cross (sub) process boundaries:
		// we don't do a processDefinition.findActivity here
		ActivityImpl sourceActivity = scope.findActivity(sourceRef);
		ActivityImpl destinationActivity = scope.findActivity(destinationRef);

		if (sourceActivity != null && destinationActivity != null) {

			TransitionImpl transition = sourceActivity.createOutgoingTransition(id);
			sequenceFlows.put(id, transition);
			transition.setProperty("name", getAttributeValue(sequenceFlowElement, "name"));
			transition.setDestination(destinationActivity);
			parseSequenceFlowConditionExpression(sequenceFlowElement, transition);
			// parseExecutionListenersOnTransition(sequenceFlowElement,
			// transition);

		} else if (sourceActivity == null) {
			throw new ActivitiException("Invalid source '" + sourceRef + "' of sequence flow '" + id + "'"
					+ sequenceFlowElement);
		} else if (destinationActivity == null) {
			throw new ActivitiException("Invalid destination '" + destinationRef + "' of sequence flow '"
					+ id + "'" + sequenceFlowElement);
		}

	}

	public void parseSequenceFlowConditionExpression(Node seqFlowElement, TransitionImpl seqFlow) {
		Node sibling = seqFlowElement.getFirstChild();
		while (sibling != null) {
			if ("conditionExpression".equals(sibling.getNodeName())) {
				break;
			}
			sibling = sibling.getNextSibling();
		}
		if (sibling == null) {
			return;
		}

		Node conditionExprElement = sibling;
		conditionExprElement = conditionExprElement.getFirstChild();
		String expression = conditionExprElement.getNodeValue().trim();
		// String type = conditionExprElement.attributeNS(BpmnParser.XSI_NS,
		// "type");
		// if (type != null && !type.equals("tFormalExpression")) {
		// addError("Invalid type, only tFormalExpression is currently supported",
		// conditionExprElement);
		// }

		// Condition expressionCondition = new
		// UelExpressionCondition(expressionManager.createExpression(expression));
		Condition expressionCondition = new JSCondition(expression);
		seqFlow.setProperty(BpmnConstants.PROPERTYNAME_CONDITION_TEXT, expression);
		seqFlow.setProperty(BpmnConstants.PROPERTYNAME_CONDITION, expressionCondition);
	}

	  public List<FieldDeclaration> parseFieldDeclarations(Node element) {
		    List<FieldDeclaration> fieldDeclarations = new ArrayList<FieldDeclaration>();
		    
		    Node elementWithFieldInjections = element.getFirstChild();
		    while(elementWithFieldInjections != null) {
		    	String nodeName = elementWithFieldInjections.getNodeName();
				if("extensionElements".equals(nodeName)) {
		    		break;
		    	}
		    	elementWithFieldInjections = elementWithFieldInjections.getNextSibling();
		    }
		    
		    if (elementWithFieldInjections == null) { // Custom extensions will just have the <field.. as a subelement
		      elementWithFieldInjections = element;
		    }

		    Node fieldDeclarationElement = elementWithFieldInjections.getFirstChild();
		    
		    final String fieldConst = BpmnConstants.ACTIVITI_PREFIX + "field";
		    while(fieldDeclarationElement != null) {
		    	String nodeName = fieldDeclarationElement.getNodeName();
				String namespaceURI = fieldDeclarationElement.getNamespaceURI();
				if(fieldConst.equals(nodeName) && BpmnConstants.ACTIVITI_BPMN_EXTENSIONS_NS.equals(namespaceURI)) {
		    		FieldDeclaration fieldDeclaration = parseFieldDeclaration(element, fieldDeclarationElement);
			        if(fieldDeclaration != null) {
			          fieldDeclarations.add(fieldDeclaration);            
			        }
		    	}
		    	fieldDeclarationElement = fieldDeclarationElement.getNextSibling();
		    }
		    
		    
		    return fieldDeclarations;
		  }

		  protected FieldDeclaration parseFieldDeclaration(Node serviceTaskElement, Node fieldDeclarationElement) {
		    String fieldName = getAttributeValue(fieldDeclarationElement, "name");
		    
		    FieldDeclaration fieldDeclaration = parseStringFieldDeclaration(fieldDeclarationElement, serviceTaskElement, fieldName);    
//		    if(fieldDeclaration == null) {
//		     fieldDeclaration = parseExpressionFieldDeclaration(fieldDeclarationElement, serviceTaskElement, fieldName);
//		    }
		    
		    if(fieldDeclaration == null) {
		      throw new ActivitiException("One of the following is mandatory on a field declaration: one of attributes stringValue|expression " + 
		        "or one of child elements string|expression" + serviceTaskElement);
		    }
		    return fieldDeclaration;
		  }
		  
		  protected FieldDeclaration parseStringFieldDeclaration(Node fieldDeclarationElement, Node serviceTaskElement, String fieldName) {
		    try {
		      String fieldValue = getStringValueFromAttributeOrElement(BpmnConstants.ACTIVITI_PREFIX + "stringValue", BpmnConstants.ACTIVITI_PREFIX + "string", null, fieldDeclarationElement);
		      if(fieldValue != null) {
//		        return new FieldDeclaration(fieldName, Expression.class.getName(), new FixedValue(fieldValue)); 
		    	  return new FieldDeclaration(fieldName, "java.lang.String", fieldValue);
		      }
		    } catch (ActivitiException ae) {
		      if (ae.getMessage().contains("multiple elements with tag name")) {
		        throw new ActivitiException("Multiple string field declarations found" + serviceTaskElement, ae);
		      } else {
		    	  throw new ActivitiException("Error when paring field declarations: " + ae.getMessage() + serviceTaskElement, ae);
		      }
		    }
		    return null;
		  }
		  
		  
//		  protected FieldDeclaration parseExpressionFieldDeclaration(Element fieldDeclarationElement, Element serviceTaskElement, String fieldName) {
//		    try {
//		      String expression = getStringValueFromAttributeOrElement("expression", "expression", null, fieldDeclarationElement);
//		      if(expression != null && expression.trim().length() > 0) {
//		        return new FieldDeclaration(fieldName, Expression.class.getName(), expressionManager.createExpression(expression));
//		      }
//		    } catch(ActivitiException ae) {
//		      if (ae.getMessage().contains("multiple elements with tag name")) {
//		        addError("Multiple expression field declarations found", serviceTaskElement);
//		      } else {
//		        addError("Error when paring field declarations: " + ae.getMessage(), serviceTaskElement);
//		      }
//		    }
//		    return null;
//		  }
		  
		  protected String getText(Node node) {
			  Node firstChild = node.getFirstChild();
			  if(Node.TEXT_NODE == firstChild.getNodeType()) {
				  return firstChild.getNodeValue();
			  }
			  return null;
		  }
		  
		  protected String getStringValueFromAttributeOrElement(String attributeName, String elementName, String namespace, Node element) {
		    String value = null;
		    
		    String attributeValue = getAttributeValue(element, attributeName);
		    Node childElement = element.getFirstChild();
		    while(childElement != null) {
		    	if(elementName.equals(childElement.getNodeName())) {
		    		break;
		    	}
		    	childElement = childElement.getNextSibling();
		    }
		    
		    String stringElementText = null;
		    
		    if(attributeValue != null && childElement != null) {
		      throw new ActivitiException("Can't use attribute '" + attributeName + "' and element '" + elementName + "' together, only use one" + element);
		    } else if (childElement != null) {
		      stringElementText = getText(childElement);
		      if (stringElementText == null || stringElementText.length() == 0) {
		        throw new ActivitiException("No valid value found in attribute '" + attributeName + "' nor element '" + elementName + "'" + element);
		      } else {
		        // Use text of element
		        value = stringElementText;
		      }
		    } else if(attributeValue != null && attributeValue.length() > 0) {
		      // Using attribute
		      value = attributeValue;
		    } 
		    
		    return value;
		  }
}
