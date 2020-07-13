package org.scharp.atlas.pepdb.view;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.labkey.api.view.JspView;

import javax.servlet.ServletException;

/**
 * @version $Id$ 
 */
public class PepDBWebPart extends JspView<Object> {

    private static Logger _log = LogManager.getLogger(PepDBWebPart.class);

    /**
     * 
     */
    public PepDBWebPart() {
        super("/org/scharp/atlas/pepdb/view/pepDBWebPart.jsp", null);
        setTitle("PepDB Web Part");
    }

    /* (non-Javadoc)
     * @see org.labkey.api.view.WebPartView#prepareWebPart(java.lang.Object)
     */
    @Override
    protected void prepareWebPart(Object object) throws ServletException {
        super.prepareWebPart(object);
        /* try {
           getViewContext().put("peptides",
                    PepDBManager.getPeptideGroups());
        } catch (SQLException e) {
            _log.error("Error retrieving list of PeptideGroups.", e);
        }  */
    }
}
