/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DroolsRulesTest;

/**
 *
 * @author jorgetb
 */
public class Product {
    
    public float price;
    public int quantity;
    public String name;
    
    public Product(float price, int quantity, String name) {
        this.price = price;
        this.quantity = quantity;
        this.name = name;
    }
    
    @Override
    public String toString() {
        String str = "";
        str += "\nPrice: " + String.valueOf(price) + "\n";
        str += "Quantity: " + String.valueOf(quantity) + "\n";
        str += "Name: " + name;
        return str;
    }
    
}
