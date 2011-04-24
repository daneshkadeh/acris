package sk.seges.acris.generator.client;

import java.util.HashSet;
import java.util.Set;

import sk.seges.acris.callbacks.client.ICallbackTrackingListener;
import sk.seges.acris.callbacks.client.RPCRequest;
import sk.seges.acris.callbacks.client.RPCRequestTracker;
import sk.seges.acris.callbacks.client.RequestState;
import sk.seges.acris.generator.client.configuration.GeneratorConfiguration;
import sk.seges.acris.generator.client.context.DefaultGeneratorClientEnvironment;
import sk.seges.acris.generator.client.context.MapTokenCache;
import sk.seges.acris.generator.client.context.api.GeneratorClientEnvironment;
import sk.seges.acris.generator.client.factory.AnchorNodeCollectorFactory;
import sk.seges.acris.generator.client.factory.api.NodeCollectorFactory;
import sk.seges.acris.generator.client.performance.OperationTimer;
import sk.seges.acris.generator.client.performance.OperationTimer.Operation;
import sk.seges.acris.generator.shared.domain.GeneratorToken;
import sk.seges.acris.generator.shared.service.IGeneratorServiceAsync;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ScriptElement;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

public abstract class GwtTestGenerateOfflineContent extends GWTTestCase {

	private HtmlFilesHandler offlineContentProvider;
	private ContentInterceptor contentProvider;

	protected GeneratorClientEnvironment generatorEnvironment;
	
	private EntryPoint site;

	private OperationTimer timer;
	protected IGeneratorServiceAsync generatorService;

	private static final boolean PERFORMANCE_MONITOR = true;
	
	/**
	 * Default timeout for whole run of offline content generator Suppose to be never expired because generator should
	 * finish in correct way - by calling finalizeTest method.
	 */
	private static final int GENERATOR_TIMEOUT = 9940000;

	public GwtTestGenerateOfflineContent() {
		super();
	}

	public String getModuleName() {
		return "sk.seges.acris.generator.Generator";
	}

	public String getName() {
		return "testLoadContent";
	}

	protected abstract EntryPoint getEntryPoint(String webId, String lang);

	protected abstract IGeneratorServiceAsync getGeneratorService();

	/**
	 * Parse a URL and return a map of query parameters. If a parameter is supplied without =value, it will be defined
	 * as null.
	 * 
	 * @param url the full or partial (ie, only location.search) URL to parse
	 * @return the map of parameter names to values
	 */
	public void testLoadContent() {

		delayTestFinish(GENERATOR_TIMEOUT);

		GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

			@Override
			public void onUncaughtException(Throwable e) {
				Log.error("Uncaught exception", e);
			}
		});
		
		GeneratorConfiguration generatorConfiguration = GWT.create(GeneratorConfiguration.class);
		
		if (PERFORMANCE_MONITOR) {
			timer = new OperationTimer();
		}

		generatorEnvironment = new DefaultGeneratorClientEnvironment(new MapTokenCache());
		GeneratorToken defaultToken = new GeneratorToken();
		defaultToken.setWebId(generatorConfiguration.getWebId());
		defaultToken.setLanguage(generatorConfiguration.getLanguage());
		generatorEnvironment.getTokensCache().setDefaultToken(defaultToken);

		prepareEnvironment(generatorConfiguration);

		generatorService = getGeneratorService();

		offlineContentProvider = new HtmlFilesHandler(getModuleName(), generatorService);

		contentProvider = getContentProvider(generatorEnvironment);

		loadTokensForProcessing();
	}

	protected Set<NodeCollectorFactory> getNodeCollectorFactories() {
		Set<NodeCollectorFactory> collectors = new HashSet<NodeCollectorFactory>();
		collectors.add(new AnchorNodeCollectorFactory());
		return collectors;
	}
	
	protected void prepareEnvironment(GeneratorConfiguration generatorConfiguration) {
		if (generatorConfiguration.getProperties() != null && generatorConfiguration.getProperties().length() > 0) {
			ScriptElement scriptElement = Document.get().createScriptElement();
			scriptElement.setAttribute("language", "JavaScript");
			scriptElement.setAttribute("src", generatorConfiguration.getProperties());
			HtmlFilesHandler.getHeadElement().appendChild(scriptElement);
		}
	};

	protected ContentInterceptor getContentProvider(GeneratorClientEnvironment generatorEnvironment) {
		return new ContentInterceptor(generatorService, generatorEnvironment);
	}

	private void loadTokensForProcessing() {

		if (PERFORMANCE_MONITOR) {
			timer.start(Operation.GENERATOR_SERVER_READ_PROCESSING);
			timer.start(Operation.GENERATOR_CLIENT_PROCESSING);
		}
		
		//Load last token for processing
		contentProvider.loadTokensForProcessing(new AsyncCallback<Void>() {

			public void onFailure(Throwable caught) {
				if (PERFORMANCE_MONITOR) {
					timer.stop(Operation.GENERATOR_SERVER_READ_PROCESSING);
				}
				failure("Unable to obtain current content. Please check the log and connectivity on the RPC server side", caught);
				finalizeTest();
			}

			public void onSuccess(Void result) {
				if (PERFORMANCE_MONITOR) {
					timer.stop(Operation.GENERATOR_SERVER_READ_PROCESSING);
				}
				if (generatorEnvironment.getTokensCache().hasNext()) {
					loadEntryPointHTML();
				} else {
					failure("No tokens available for processing. Finishing", null);
					finalizeTest();
				}
			}
		});

		if (PERFORMANCE_MONITOR) {
			timer.stop(Operation.GENERATOR_CLIENT_PROCESSING);
		}
	}

	private void loadEntryPointHTML() {

		if (PERFORMANCE_MONITOR) {
			timer.start(Operation.GENERATOR_CLIENT_PROCESSING);
		}
		
		//Load entry point
		offlineContentProvider.getEntryPointBodyHtml(new AsyncCallback<String>() {

			public void onFailure(Throwable caught) {
				if (PERFORMANCE_MONITOR) {
					timer.stop(Operation.GENERATOR_SERVER_READ_PROCESSING);
				}
				failure("Unable to load entry point", caught);
				finalizeTest();
			}

			public void onSuccess(String result) {
				if (PERFORMANCE_MONITOR) {
					timer.stop(Operation.GENERATOR_SERVER_READ_PROCESSING);
					timer.start(Operation.GENERATOR_CLIENT_PROCESSING);
				}
				UIHelper.cleanUI();
				RootPanel.get().getElement().setInnerHTML(result);
				if (PERFORMANCE_MONITOR) {
					timer.stop(Operation.GENERATOR_CLIENT_PROCESSING);
				}
				loadNextContent();
			}
		});
		
		if (PERFORMANCE_MONITOR) {
			timer.stop(Operation.GENERATOR_CLIENT_PROCESSING);
			timer.start(Operation.GENERATOR_SERVER_READ_PROCESSING);
		}
	}

	private GeneratorToken loadNextContent() {
		if (!generatorEnvironment.getTokensCache().hasNext()) {
			finalizeTest();
			return null;
		}

		if (PERFORMANCE_MONITOR) {
			timer.start(Operation.CONTENT_GENERATING);
			timer.start(Operation.GENERATOR_CLIENT_PROCESSING);
		}
		
		GeneratorToken generatorToken = generatorEnvironment.getTokensCache().next();
		
		Log.info("Generating offline content for niceurl [" + (generatorEnvironment.getTokensCache().getTokensCount() - 
				generatorEnvironment.getTokensCache().getWaitingTokensCount()) + " / " + generatorEnvironment.getTokensCache().getTokensCount() + "]: " + 
				generatorToken.getNiceUrl());

		RPCRequestTracker.getTracker().registerCallbackListener(new ICallbackTrackingListener() {

			@Override
			public void onProcessingFinished(RPCRequest request) {
				if (request.getCallbackResult().equals(RequestState.REQUEST_FAILURE)) {
					if (PERFORMANCE_MONITOR) {
						timer.stop(Operation.CONTENT_RENDERING);
						timer.stop(Operation.CONTENT_GENERATING);
					}
					failure("Unable to load content. See the previous errors in console.", null);
					RPCRequestTracker.getTracker().removeAllCallbacks();
					loadNextContent();
					//finalizeTest();
				} else {
					if (request.getParentRequest() == null) {
						if (PERFORMANCE_MONITOR) {
							timer.stop(Operation.CONTENT_RENDERING);
						}
						
						RPCRequestTracker.getTracker().removeAllCallbacks();
						loadContentForToken();
					}
				}
			}

			@Override
			public void onRequestStarted(RPCRequest request) {
			}

			@Override
			public void onResponseReceived(RPCRequest request) {
			}

		});

		if (PERFORMANCE_MONITOR) {
			timer.stop(Operation.GENERATOR_CLIENT_PROCESSING);
		}
		
		if (site == null) {
			site = getEntryPoint(generatorToken.getWebId(), generatorToken.getLanguage());
			if (PERFORMANCE_MONITOR) {
				timer.start(Operation.CONTENT_RENDERING);
			}
			site.onModuleLoad();
			
			generatorEnvironment.setServerURL(GWT.getHostPageBaseURL().replaceAll(GWT.getModuleName() + "/", ""));
		} else {
			RPCRequestTracker.getTracker().removeAllCallbacks();
			loadContentForToken();
		}

		return generatorToken;
	}

	private void loadContentForToken() {
		if (PERFORMANCE_MONITOR) {
			timer.start(Operation.CONTENT_RENDERING);
		}
		
		contentProvider.loadContent(new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				if (PERFORMANCE_MONITOR) {
					timer.stop(Operation.CONTENT_RENDERING);
					timer.stop(Operation.CONTENT_GENERATING);
				}
				
				failure("Unable to load content for nice-url " + generatorEnvironment.getTokensCache().getCurrentToken().getNiceUrl() + ".", caught);
				loadNextContent();
			}

			@Override
			public void onSuccess(Void result) {
				if (PERFORMANCE_MONITOR) {
					timer.stop(Operation.CONTENT_RENDERING);
				}
				saveAndLoadContent(generatorEnvironment);
			}
		});
		
		if (PERFORMANCE_MONITOR) {
			timer.stop(Operation.GENERATOR_CLIENT_PROCESSING);
			timer.start(Operation.GENERATOR_CLIENT_PROCESSING);
		}
	}

	private void saveAndLoadContent(final GeneratorClientEnvironment generatorEnvironment) {

		if (PERFORMANCE_MONITOR) {
			timer.start(Operation.GENERATOR_DOM_MANIPULATION);
		}

		com.google.gwt.user.client.Element rootElement = contentProvider.getRootElement();

		for (NodeCollectorFactory nodeCollectorFactory: getNodeCollectorFactories()) {
			nodeCollectorFactory.create().collect(rootElement, generatorEnvironment);
		}
		
		String content = DOM.getInnerHTML(rootElement);
		
		Log.info("Content length: " + content.length());
		
		if (PERFORMANCE_MONITOR) {
			timer.stop(Operation.GENERATOR_DOM_MANIPULATION);
			timer.start(Operation.GENERATOR_CLIENT_PROCESSING);
		}
		
		offlineContentProvider.saveOfflineContent(content, generatorEnvironment.getTokensCache().getCurrentToken(), 
				generatorEnvironment.getServerURL(), new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {
				if (PERFORMANCE_MONITOR) {
					timer.stop(Operation.CONTENT_GENERATING);
					Log.info(timer.report());
					timer.stop(Operation.GENERATOR_CLIENT_PROCESSING);
				}
				loadNextContent();
			}

			@Override
			public void onSuccess(Void result) {
				if (PERFORMANCE_MONITOR) {
					timer.stop(Operation.CONTENT_GENERATING);
					Log.info(timer.report());
					timer.stop(Operation.GENERATOR_CLIENT_PROCESSING);
				}
				loadNextContent();
			}
		});
	}

	private void failure(String msg, Throwable caught) {
		Log.error(msg);
	}

	protected void finalizeEnvironment() {
		UIHelper.cleanUI();
	}

	private void finalizeTest() {
		if (PERFORMANCE_MONITOR) {
			Log.info(timer.report());
		}
		finalizeEnvironment();
		finishTest();
	}
}