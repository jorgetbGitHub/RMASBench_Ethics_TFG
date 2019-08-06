/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DroolsRulesTest;
import DroolsRulesTest.Product;

import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.impl.KnowledgeBaseFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.kie.api.io.ResourceType;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;


/**
 *
 * @author jorgetb
 */
public class DroolsTest {
    
    public DroolsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void hello() {}
    
    @Test
    public void testLimitQuantity() {
        String str = "";
        str += "package DroolsRulesTest;";
        str += "rule maxQuantity \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "    p : Product ( quantity > 100) \n";
        str += "then \n";
        //str += "    System.out.println(" + "Quantity equal 100" + ");\n";
        str += "    modify(p) { quantity = 100; } \n";
        str += "end \n";
        
        System.out.println(str);
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str.getBytes()), ResourceType.DRL);
        
        if (kbuilder.hasErrors()) {
            System.out.println(kbuilder.getErrors().toString());
        }
        
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages(kbuilder.getKnowledgePackages());
        
        StatefulKnowledgeSession ksession = (StatefulKnowledgeSession) kbase.newKieSession();
        Product product = new Product(10, 1000, "Jamón");
        ksession.insert(product);
        product.quantity = 105;
        
        System.out.println("Quantity BEFORE fire rule -> " + String.valueOf(product.quantity));
        ksession.fireAllRules();
        System.out.println("Quantity AFTER fire rule -> " + String.valueOf(product.quantity));
        
        //ksession.dispose();
        
        //product.quantity += 100;
        //ksession.insert(product);
       
        assertEquals(100, product.quantity);
        
        /*product.quantity = 5;
        ksession.fireAllRules();
        System.out.println(product);
        product.quantity += 100;
        //ksession.dispose();
        ksession.fireAllRules();
        System.out.println("\nMust have 100 as max quantity but the product is... \n" + product +"\n");
        
        
        ksession.insert(product);
        ksession.fireAllRules();
        System.out.println(product);
        
        Product product2 = new Product(10, product.quantity, "Etc");
        System.out.println("product2 = " + product2);
        ksession.insert(product2);
        ksession.fireAllRules();
        System.out.println("product = " + product);
        System.out.println("product2 = " + product2);*/
    }
    
    @Test
    public void testMultipleLimitQuantity() {
        String str = "";
        str += "package DroolsRulesTest;";
        str += "rule maxQuantity \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "    p : Product ( quantity > 100) \n";
        str += "then \n";
        //str += "    System.out.println(" + "Quantity equal 100" + ");\n";
        str += "    modify(p) { quantity = 100; } \n";
        str += "end \n";
        
        System.out.println(str);
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str.getBytes()), ResourceType.DRL);
        
        if (kbuilder.hasErrors()) {
            System.out.println(kbuilder.getErrors().toString());
        }
        
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages(kbuilder.getKnowledgePackages());
        
        StatefulKnowledgeSession ksession = (StatefulKnowledgeSession) kbase.newKieSession();
        Product product1 = new Product(10, 1000, "Jamón");
        Product product2 = new Product(20, 2000, "Etc");
        ksession.insert(product1);
        ksession.insert(product2);
        
        ksession.fireAllRules();
        
        assertEquals(100, product1.quantity);
        assertEquals(100, product2.quantity);
    }
    
    @Test
    public void testMultipleRulesOverDifferentObjects() {
        String str = "";
        str += "package DroolsRulesTest;";
        
        str += "rule minQuantity \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "    p : Product ( quantity < 50) \n";
        str += "then \n";
        str += "    modify(p) { quantity = 50; } \n";
        str += "end \n";
        
        str += "rule maxQuantity \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "    p : Product ( quantity > 100) \n";
        str += "then \n";
        str += "    modify(p) { quantity = 100; } \n";
        str += "end \n";
        
        System.out.println(str);
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str.getBytes()), ResourceType.DRL);
        
        if (kbuilder.hasErrors()) {
            System.out.println(kbuilder.getErrors().toString());
        }
        
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages(kbuilder.getKnowledgePackages());
        
        StatefulKnowledgeSession ksession = (StatefulKnowledgeSession) kbase.newKieSession();
        
        Product product1 = new Product(10, 25, "Prod1");
        Product product2 = new Product(10, 125, "Prod2");
        
        ksession.insert(product1);
        ksession.insert(product2);
        
        ksession.fireAllRules();
        
        System.out.println("Prod1 after fire all rules -> " + product1);
        System.out.println("Prod2 after fire all rules -> " + product2);
        
        assertEquals(product1.quantity, 50);
        assertEquals(product2.quantity, 100);
    }
    
    @Test
    public void testMultipleRulesOverSameObject() {
        String str = "";
        str += "package DroolsRulesTest;";
        
        str += "rule minQuantity \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "    p : Product ( quantity < 50) \n";
        str += "then \n";
        str += "    modify(p) { quantity = 50; } \n";
        str += "end \n";
        
        str += "rule maxQuantity \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "    p : Product ( quantity > 100) \n";
        str += "then \n";
        str += "    modify(p) { quantity = 100; } \n";
        str += "end \n";
        
        System.out.println(str);
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str.getBytes()), ResourceType.DRL);
        
        if (kbuilder.hasErrors()) {
            System.out.println(kbuilder.getErrors().toString());
        }
        
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages(kbuilder.getKnowledgePackages());
        
        StatefulKnowledgeSession ksession = (StatefulKnowledgeSession) kbase.newKieSession();
        
        Product product1 = new Product(10, 25, "Prod1");
        
        ksession.insert(product1);
        ksession.fireAllRules();
        
        System.out.println("Product1 after fire all rules -> " + product1);
        
        product1.quantity += 100;
        
        System.out.println("Product1 modified -> " + product1);
        
        ksession.fireAllRules();
        
        System.out.println("Product1 after fire all rules (second time) ->" + product1);
        
        assertEquals(150, product1.quantity);
    }
    
    @Test
    public void testReinsertObject() {
        String str = "";
        str += "package DroolsRulesTest;";
        
        str += "rule minQuantity \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "    p : Product ( quantity < 50) \n";
        str += "then \n";
        str += "    modify(p) { quantity = 50; } \n";
        str += "end \n";
        
        str += "rule maxQuantity \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "    p : Product ( quantity > 100) \n";
        str += "then \n";
        str += "    modify(p) { quantity = 100; } \n";
        str += "end \n";
        
        System.out.println(str);
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str.getBytes()), ResourceType.DRL);
        
        if (kbuilder.hasErrors()) {
            System.out.println(kbuilder.getErrors().toString());
        }
        
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages(kbuilder.getKnowledgePackages());
        
        StatefulKnowledgeSession ksession = (StatefulKnowledgeSession) kbase.newKieSession();
        
        Product product = new Product(10, 25, "Prod1");
        ksession.insert(product);
        ksession.fireAllRules();
        
        System.out.println("After fire all rules (before 25 quantity), now -> " + product);
        
        assertEquals(50, product.quantity);
        
        ksession.delete(ksession.getFactHandle(product));
        product.quantity = 150;
        
        ksession.insert(product);
        ksession.fireAllRules();
        
        System.out.println("After fire all rules (second time, 150 quantity), now -> " + product);
        
        assertEquals(100, product.quantity);
    }
    
    @Test
    public void testGlobalVariablesDrools() {
        String str = "";
        str += "package DroolsRulesTest;";
        str += "global String filterName;";
        
        str += "rule minQuantity \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "    p : Product ( quantity < 50 && name.equals(filterName) ) \n";
        str += "then \n";
        str += "    modify(p) { quantity = 50; } \n";
        str += "end \n";
        
        str += "rule maxQuantity \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "    p : Product ( quantity > 100 && name.equals(filterName) ) \n";
        str += "then \n";
        str += "    modify(p) { quantity = 100; } \n";
        str += "end \n";
        
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str.getBytes()), ResourceType.DRL);
        
        if (kbuilder.hasErrors()) {
            System.out.println(kbuilder.getErrors().toString());
        }
        
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages(kbuilder.getKnowledgePackages());
        
        StatefulKnowledgeSession ksession = (StatefulKnowledgeSession) kbase.newKieSession();
        
        Product product1 = new Product(10, 25, "Prod1");
        Product product2 = new Product(20, 125, "Prod2");
        
        ksession.setGlobal("filterName", product1.name);
        ksession.insert(product1);
        
        ksession.fireAllRules();
        
        ksession.setGlobal("filterName", product2.name);
        ksession.insert(product2);
        
        ksession.fireAllRules();
        
        System.out.println("[testGlobalVariablesDrools] product1 = " + product1 + " product2 = " + product2);
        assertEquals(false, kbuilder.hasErrors());
        assertEquals(50, product1.quantity);
        assertEquals(100, product2.quantity);
    }
    
    @Test
    public void testGlobalVariablesDrools2() {
        String str = "";
        str += "package DroolsRulesTest;";
        str += "global String filterName;";
        
        str += "rule minQuantity \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "    p : Product ( quantity < 50 && name.equals(filterName) ) \n";   
        str += "then \n";
        str += "    modify(p) { quantity = 50; } \n";
        str += "end \n";
        
        str += "rule maxQuantity \n";
        str += "    dialect \"java\" \n";
        str += "when \n";
        str += "    p : Product ( quantity > 100 && name.equals(filterName) ) \n";
        str += "then \n";
        str += "    modify(p) { quantity = 100; } \n";
        str += "end \n";
        
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newByteArrayResource(str.getBytes()), ResourceType.DRL);
        
        if (kbuilder.hasErrors()) {
            System.out.println(kbuilder.getErrors().toString());
        }
        
        InternalKnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addPackages(kbuilder.getKnowledgePackages());
        
        StatefulKnowledgeSession ksession = (StatefulKnowledgeSession) kbase.newKieSession();
        
        Product product1 = new Product(10, 25, "Prod1");
        Product product2 = new Product(20, 125, "Prod2");
      
        ksession.setGlobal("filterName", product1.name);
        ksession.insert(product1);
        ksession.insert(product2);
        
        ksession.fireAllRules();
        
        ksession.setGlobal("filterName", product2.name);
        
        ksession.fireAllRules();
        
        System.out.println("[testGlobalVariablesDrools] product1 = " + product1 + " product2 = " + product2);
        assertEquals(false, kbuilder.hasErrors());
        assertEquals(50, product1.quantity);
        assertEquals(125, product2.quantity);
    }
}
