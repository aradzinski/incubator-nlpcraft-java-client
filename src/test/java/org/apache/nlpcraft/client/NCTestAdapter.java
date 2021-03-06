/*
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

package org.apache.nlpcraft.client;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * REST client test adapter.
 */
class NCTestAdapter {
    /**
     *
     */
    interface TestRunnable {
        /**
         *
         * @throws Exception
         */
        void run() throws Exception;
    }
    
    /** */
    protected NCClient admCli;
    
    /** */
    protected long admUsrId;
    
    /**
     *
     * @throws Exception
     */
    @BeforeEach
    void setUp() throws Exception {
        admCli = new NCClientBuilder().build();
        
        admUsrId = get(admCli.getAllUsers(), (u) -> NCClientBuilder.DFLT_EMAIL.equals(u.getEmail())).getId();
        
        clearAll();
    }
    
    /**
     *
     * @throws Exception
     */
    @AfterEach
    void tearDown() throws Exception {
        if (admCli != null) {
            // Clears all.
            clearAll();
    
            admCli.close();
        }
    }
    
    /**
     *
     * @throws IOException
     */
    private void clearAll() throws IOException {
        admCli.deleteFeedback(null);
        admCli.cancel(null, null, null);
        admCli.deleteUser(null, null);
    }
    
    /**
     *
     * @param r
     * @param claxx
     */
    protected void testException(TestRunnable r, Class<?> claxx) {
        try {
            r.run();
            
            fail("Operation shouldn't passed.");
        }
        catch (Exception e) {
            if (!claxx.isAssignableFrom(e.getClass()))
                throw new RuntimeException(e);
            
            System.out.println("Expected error: " + e.getMessage() + ", type=" + e.getClass().getName());
        }
    }
    
    /**
     *
     * @param r
     * @param expCode
     * @throws IOException
     */
    protected void testServerException(TestRunnable r, String expCode) throws Exception {
        try {
            r.run();
            
            fail("Operation shouldn't passed.");
        }
        catch (NCClientException e) {
            assertEquals(expCode, e.getServerCode(), "Error: " + e.getLocalizedMessage());
            
            System.out.println("Expected error: " + e.getMessage() + ", code=" + expCode);
        }
    }
    
    /**
     *
     * @param list
     * @param p
     * @return
     */
    protected<T> Optional<T> getOpt(List<T> list, Predicate<T> p) {
        return list.stream().filter(p).findAny();
    }
    
    /**
     *
     * @param list
     * @param p
     * @return
     */
    protected<T> T get(List<T> list, Predicate<T> p) {
        Optional<T> opt = getOpt(list, p);
    
        if (!opt.isPresent())
            fail("Object not found");
    
        return opt.get();
    }
    
    /**
     *
     * @param state
     */
    protected void checkOk(NCResult state) {
        System.out.println(
            String.format(
                "Text: %s \ntype: %s\nresult: %s",
                state.getText(),
                state.getResultType(),
                state.getResultBody()
            )
        );
    
        if (state.getLogHolder() != null)
            System.out.printf("log:\n%s%n", state.getLogHolder());
        
        assertNotNull(state.getResultBody(), "Error: " + state.getErrorMessage());
        assertNotNull(state.getResultType());
        assertNull(state.getErrorMessage());
        assertNull(state.getErrorCode());
    }
    
    /**
     *
     * @param state
     */
    protected void checkError(NCResult state) {
        assert state != null;
        
        System.out.println(
            String.format(
                "Text: %s \nerror: %s\ncode: %d",
                state.getText(),
                state.getErrorMessage(),
                state.getErrorCode()
            )
        );
    
        if (state.getLogHolder() != null)
            System.out.printf("log:\n%s%n", state.getLogHolder());
    
        assertNull(state.getResultBody());
        assertNull(state.getResultType());
        assertNotNull(state.getErrorMessage());
        assertNotNull(state.getErrorCode());
    }
}
