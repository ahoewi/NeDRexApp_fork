package org.cytoscape.myApp.internal.io;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
/**
 * NeDRex App
 * @author Sepideh Sadegh
 */
public class HttpGetWithEntity extends HttpEntityEnclosingRequestBase {
    public final static String METHOD_NAME = "GET";

    @Override
    public String getMethod() {
        return METHOD_NAME;
    }
}
