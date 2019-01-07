package org.trading.trade.execution.esp.web;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.slf4j.Logger;
import org.trading.api.event.LimitOrderPlaced;
import org.trading.api.event.TradeExecuted;
import org.trading.messaging.Message;
import org.trading.messaging.netty.TcpDataSource;
import org.trading.trade.execution.esp.HedgingEventHandler;
import org.trading.trade.execution.esp.ReportExecutionEventHandler;
import org.trading.trade.execution.esp.TradeMessage;
import org.trading.trade.execution.esp.TradePublisher;
import org.trading.trade.execution.esp.domain.ExecutionRequest;
import org.trading.trade.execution.esp.domain.LastLook;
import org.trading.trade.execution.esp.domain.TradeListener;
import org.trading.trade.execution.esp.translate.ExecutionRequestTranslator;
import org.trading.trade.execution.esp.web.json.ExecutionRequestFromJson;
import org.trading.web.SseEventDispatcher;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.lmax.disruptor.dsl.ProducerType.SINGLE;
import static com.lmax.disruptor.util.DaemonThreadFactory.INSTANCE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static net.minidev.json.parser.JSONParser.MODE_RFC4627;
import static org.eclipse.jetty.http.MimeTypes.Type.APPLICATION_JSON;
import static org.slf4j.LoggerFactory.getLogger;
import static org.trading.messaging.Message.EventType.SUBSCRIBE;
import static org.trading.messaging.Message.FACTORY;

public class ExecutionServlet extends HttpServlet implements EventHandler<Message> {
    private final static Logger LOGGER = getLogger(ExecutionServlet.class);
    private final SseEventDispatcher eventDispatcher = new SseEventDispatcher();
    private final ExecutionRequestFromJson executionRequestFromJson = new ExecutionRequestFromJson();
    private final String host;
    private final int port;
    private Disruptor<Message> disruptor;
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
    private LastLook lastLook;
    private final Disruptor<Message> executionDisruptor;

    public ExecutionServlet(String host, int port, Disruptor<Message> disruptor) {
        this.host = host;
        this.port = port;
        this.executionDisruptor = disruptor;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        disruptor = new Disruptor<>(FACTORY, 1024, INSTANCE, SINGLE, new BlockingWaitStrategy());
        disruptor.handleEventsWith(this);
        disruptor.start();
        TcpDataSource dataSource = new TcpDataSource(host, port, disruptor, "Execution");
        dataSource.connect();

        Disruptor<TradeMessage> outboundDisruptor = new Disruptor<>(
                TradeMessage.FACTORY,
                1024,
                INSTANCE,
                SINGLE,
                new BlockingWaitStrategy()
        );

        TradeListener tradeListener = new TradePublisher(outboundDisruptor);

        outboundDisruptor.handleEventsWith(
                new ReportExecutionEventHandler(eventDispatcher),
                new HedgingEventHandler(executionDisruptor)
        );
        lastLook = new LastLook(tradeListener, 0.01);
        outboundDisruptor.start();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        eventDispatcher.register(req, resp);
        disruptor.publishEvent((event, sequence) -> event.type = SUBSCRIBE);
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            JSONParser parser = new JSONParser(MODE_RFC4627);
            final JSONObject jsonObject = (JSONObject) parser.parse(req.getReader());
            LOGGER.info(jsonObject.toJSONString());
            ExecutionRequest executionRequest = executionRequestFromJson.toJson(jsonObject);
            executorService.submit(() -> {
                executorService.schedule(() -> disruptor.publishEvent(ExecutionRequestTranslator::translateTo, executionRequest), 300, MILLISECONDS);
            });
            resp.setContentType(APPLICATION_JSON.asString());
            resp.setStatus(SC_OK);
        } catch (ParseException e) {
            throw new ServletException("Invalid request", e);
        }
    }

    @Override
    public void onEvent(org.trading.messaging.Message message, long sequence, boolean endOfBatch) {
        LOGGER.info("Last look {}", message.toString());

        org.trading.messaging.Message.EventType eventType = message.type;
        switch (eventType) {
            case SUBSCRIBE:
                lastLook.reportExecutions();
                break;
            case LIMIT_ORDER_PLACED:
                LimitOrderPlaced limitOrderPlaced = ((LimitOrderPlaced) message.event);
                lastLook.onLimitOrderPlaced(limitOrderPlaced);
                break;
            case TRADE_EXECUTED:
                TradeExecuted tradeExecuted = ((TradeExecuted) message.event);
                lastLook.onTradeExecuted(tradeExecuted);
                break;
            case REQUEST_EXECUTION:
                lastLook.requestExecution((ExecutionRequest) message.event);
                break;
            default:
                break;
        }

    }
}