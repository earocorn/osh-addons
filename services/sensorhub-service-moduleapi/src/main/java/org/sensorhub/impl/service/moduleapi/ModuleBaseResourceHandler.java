/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are subject to the Mozilla Public License, v. 2.0.
 If a copy of the MPL was not distributed with this file, You can obtain one
 at http://mozilla.org/MPL/2.0/.

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.

 Copyright (C) 2020 Sensia Software LLC. All Rights Reserved.

 ******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.impl.service.moduleapi;

import java.io.IOException;

import org.sensorhub.impl.service.consys.BaseHandler;
import org.sensorhub.impl.service.consys.ServiceErrors;
import org.sensorhub.impl.service.consys.resource.RequestContext;
import org.sensorhub.impl.service.consys.resource.IResourceHandler;


/**
 * <p>
 * Base resource handler, maintaining connection to a datastore
 * </p>
 *
 * @author Alex Robin
 * @date Nov 15, 2018
 */
public abstract class ModuleBaseResourceHandler extends BaseHandler implements IResourceHandler
{
    public static final String READ_ONLY_ERROR = "Resource type is read-only";
    public static final String NOT_IMPLEMENTED_ERROR = "Not implemented";
    public static final String INVALID_TIMESTAMP_ERROR_MSG = "Invalid time stamp: ";
    public static final String ALREADY_EXISTS_ERROR_MSG = "Resource already exists";
    public static final String STREAMING_UNSUPPORTED_ERROR_MSG = "Streaming not supported on this resource collection";
    public static final String EVENTS_UNSUPPORTED_ERROR_MSG = "Events not supported on this resource collection";

    protected boolean readOnly = false;

    @Override
    public void doGet(final RequestContext ctx) throws IOException
    {
        // if requesting from this resource collection
        if (ctx.isEndOfPath())
        {
            list(ctx);
            return;
        }

        // otherwise there should be a specific resource ID or 'count'
        String id = ctx.popNextPathElt();
        if (ctx.isEndOfPath())
        {
            getById(ctx, id);
            return;
        }

        // next should be nested resource
        IResourceHandler resource = getSubResource(ctx);
        if (resource != null)
            resource.doGet(ctx);
        else
            throw ServiceErrors.badRequest(INVALID_URI_ERROR_MSG);
    }


    @Override
    public void doPost(final RequestContext ctx) throws IOException
    {
        if (!ctx.isEndOfPath())
        {
            // next should be resource ID
            ctx.popNextPathElt();
            if (ctx.isEndOfPath())
                throw ServiceErrors.unsupportedOperation("Can only POST on collections");

            // next should be nested resource
            IResourceHandler resource = getSubResource(ctx);
            if (resource != null)
                resource.doPost(ctx);
            else
                throw ServiceErrors.badRequest(INVALID_URI_ERROR_MSG);
        }
        else
            create(ctx);
    }


    @Override
    public void doPut(final RequestContext ctx) throws IOException
    {
        if (ctx.isEndOfPath())
            throw ServiceErrors.unsupportedOperation("Can only PUT on specific resource");

        // next should be resource ID
        String id = ctx.popNextPathElt();

        if (!ctx.isEndOfPath())
        {
            // next should be nested resource
            IResourceHandler resource = getSubResource(ctx);
            if (resource != null)
                resource.doPut(ctx);
            else
                throw ServiceErrors.badRequest(INVALID_URI_ERROR_MSG);
        }
        else
            update(ctx, id);
    }


    @Override
    public void doDelete(final RequestContext ctx) throws IOException
    {
        if (ctx.isEndOfPath())
            throw ServiceErrors.unsupportedOperation("Can only DELETE a specific resource");

        // next should be resource ID
        String id = ctx.popNextPathElt();

        if (!ctx.isEndOfPath())
        {
            // next should be nested resource
            IResourceHandler resource = getSubResource(ctx);
            if (resource != null)
                resource.doDelete(ctx);
            else
                throw ServiceErrors.badRequest(INVALID_URI_ERROR_MSG);
        }
        else
            delete(ctx, id);
    }


    protected abstract void getById(final RequestContext ctx, String id) throws IOException;

    protected abstract void list(final RequestContext ctx) throws IOException;

    protected abstract void create(final RequestContext ctx) throws IOException;

    protected abstract void update(final RequestContext ctx, final String id) throws IOException;

    protected abstract void delete(final RequestContext ctx, final String id) throws IOException;
}
