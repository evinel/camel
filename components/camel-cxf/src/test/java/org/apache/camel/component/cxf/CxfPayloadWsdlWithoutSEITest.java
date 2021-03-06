/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.cxf;
import org.junit.Before;

import javax.xml.ws.Endpoint;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.cxf.common.message.CxfConstants;
import org.apache.camel.wsdl_first.PersonImpl;
import org.apache.cxf.binding.soap.SoapFault;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class CxfPayloadWsdlWithoutSEITest extends AbstractCxfWsdlFirstTest {

    @BeforeClass
    public static void startService() {
        Object implementor = new PersonImpl();
        String address = "http://localhost:" + getPort1() + "/CxfPayloadWsdlWithoutSEITest/PersonService/";
        Endpoint.publish(address, implementor);
    }

    @Override
    protected ClassPathXmlApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext("org/apache/camel/component/cxf/CxfPayloadWsdlWithoutSEI.xml");
    }

    @Test
    @Override
    public void testInvokingServiceWithCamelProducer() {
        Exchange exchange = sendJaxWsMessage("hello");
        assertEquals("The request should be handled sucessfully ", exchange.isFailed(), false);
        org.apache.camel.Message out = exchange.getOut();
        String result =  out.getBody(String.class);
        assertStringContains(result, "Bonjour");

        exchange = sendJaxWsMessage("");
        assertEquals("We should get a fault here", exchange.isFailed(), true);
        Throwable ex = exchange.getException();
        assertTrue("We should get a SoapFault here", ex instanceof SoapFault);
    }

    private Exchange sendJaxWsMessage(final String personIdString) {
        Exchange exchange = template.send("direct:producer", new Processor() {
            public void process(final Exchange exchange) {
                String body = "<GetPerson xmlns=\"http://camel.apache.org/wsdl-first/types\"><personId>" + personIdString + "</personId></GetPerson>\n";
                exchange.getIn().setBody(body);
                exchange.getIn().setHeader(CxfConstants.OPERATION_NAME, "GetPerson");
            }
        });
        return exchange;
    }

}
