package org.scharp.atlas.pepdb;

import org.labkey.api.module.DefaultModule;
import org.labkey.api.module.ModuleContext;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.Container;
import org.labkey.api.data.DbSchema;
import org.labkey.api.view.*;
import org.labkey.api.util.PageFlowUtil;
import org.scharp.atlas.pepdb.view.PepDBWebPart;
import org.apache.log4j.Logger;

import java.util.*;
import java.lang.reflect.InvocationTargetException;

public class PepDBModule extends DefaultModule
{
    private static final Logger _log = Logger.getLogger(DefaultModule.class);
    public static final String NAME = "PepDB";

    public String getName()
    {
        return NAME;
    }

    public double getVersion()
    {
        return 2.21;
    }

    protected void init()
    {
       addController("pepdb", PepDBController.class);
    }

    protected Collection<WebPartFactory> createWebPartFactories()
    {
        return new ArrayList<WebPartFactory>(Arrays.asList(new BaseWebPartFactory("PepDB Summary") {
                public WebPartView getWebPartView(ViewContext portalCtx, Portal.WebPart webPart) throws IllegalAccessException, InvocationTargetException
                {
                    return new PepDBWebPart();
                }
            },
            new BaseWebPartFactory("PepDB Summary", WebPartFactory.LOCATION_RIGHT) {
                {
                    addLegacyNames("Narrow PepDB Summary");
                }

                public WebPartView getWebPartView(ViewContext portalCtx, Portal.WebPart webPart) throws IllegalAccessException, InvocationTargetException
                {
                    return new PepDBWebPart();
                }
            }));
    }
    
    public boolean hasScripts()
    {
        return true;
    }

    public Collection<String> getSummary(Container c)
    {
        return Collections.emptyList();
    }

    public void doStartup(ModuleContext moduleContext)
    {
        // add a container listener so we'll know when our container is deleted:
        ContainerManager.addContainerListener(new PepDBContainerListener());
    }
    public Set<String> getSchemaNames()
    {
        return PageFlowUtil.set(PepDBSchema.getInstance().getSchemaName());
    }
    public Set<DbSchema> getSchemasToTest()
    {
        return PageFlowUtil.set(PepDBSchema.getInstance().getSchema());
    }
}
