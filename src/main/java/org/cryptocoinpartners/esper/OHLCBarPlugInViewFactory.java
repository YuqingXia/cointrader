package org.cryptocoinpartners.esper;

import java.util.List;

import org.cryptocoinpartners.schema.Tradeable;

import com.espertech.esper.client.EventType;
import com.espertech.esper.core.context.util.AgentInstanceViewFactoryChainContext;
import com.espertech.esper.core.service.StatementContext;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.view.View;
import com.espertech.esper.view.ViewFactory;
import com.espertech.esper.view.ViewFactoryContext;
import com.espertech.esper.view.ViewFactorySupport;
import com.espertech.esper.view.ViewParameterException;

public class OHLCBarPlugInViewFactory extends ViewFactorySupport {
    private ViewFactoryContext viewFactoryContext;
    private List<ExprNode> viewParameters;
    private ExprNode timestampExpression;
    private ExprNode valueExpression;
    private ExprNode volumeExpression;
    private ExprNode marketExpression;
    private ExprNode intervalExpression;

    @Override
    public void setViewParameters(ViewFactoryContext viewFactoryContext, List<ExprNode> viewParameters) throws ViewParameterException {
        this.viewFactoryContext = viewFactoryContext;
        if (viewParameters.size() < 2) {
            throw new ViewParameterException(
                    "View requires at least two parameters: the expression returning timestamps and the expression supplying OHLC data points");
        }
        this.viewParameters = viewParameters;
    }

    @Override
    public void attach(EventType parentEventType, StatementContext statementContext, ViewFactory optionalParentFactory, List<ViewFactory> parentViewFactories)
            throws ViewParameterException {
        ExprNode[] validatedNodes = ViewFactorySupport.validate("OHLC view", parentEventType, statementContext, viewParameters, false);

        timestampExpression = validatedNodes[0];
        valueExpression = validatedNodes[1];
        volumeExpression = validatedNodes[2];
        marketExpression = validatedNodes[3];
        intervalExpression = validatedNodes[4];

        if ((timestampExpression.getExprEvaluator().getType() != long.class) && (timestampExpression.getExprEvaluator().getType() != Long.class)) {
            throw new ViewParameterException("View requires long-typed timestamp values in parameter 1");
        }
        if ((valueExpression.getExprEvaluator().getType() != double.class) && (valueExpression.getExprEvaluator().getType() != Double.class)) {
            throw new ViewParameterException("View requires double-typed values for in parameter 2");
        }
        if ((volumeExpression.getExprEvaluator().getType() != double.class) && (volumeExpression.getExprEvaluator().getType() != Double.class)) {
            throw new ViewParameterException("View requires double-typed values for in parameter 3");
        }
        if ((marketExpression.getExprEvaluator().getType() != Tradeable.class) && (marketExpression.getExprEvaluator().getType() != Tradeable.class)) {
            throw new ViewParameterException("View requires tradeable-typed values for in parameter 4");
        }
        if ((intervalExpression.getExprEvaluator().getType() != double.class) && (intervalExpression.getExprEvaluator().getType() != Double.class)) {
            throw new ViewParameterException("View requires double-typed values for in parameter 5");
        }
    }

    @Override
    public View makeView(AgentInstanceViewFactoryChainContext agentInstanceViewFactoryContext) {
        return new OHLCBarPlugInView(agentInstanceViewFactoryContext, timestampExpression, valueExpression, volumeExpression, marketExpression,
                intervalExpression);
    }

    @Override
    public EventType getEventType() {
        return OHLCBarPlugInView.getEventType(viewFactoryContext.getEventAdapterService());
    }

    public String getViewName() {
        return OHLCBarPlugInView.class.getSimpleName();
    }
}
