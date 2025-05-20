package model;

import java.io.Serializable;

public class Cashier implements Serializable {
    private final int id;
    private final String name;
    private final double salary;

    public Cashier(int id, String name, double salary) {
        this.id = id;
        this.name = name;
        this.salary = salary;
    }

    public int getId() { return id; }
    public double getSalary() { return salary; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return String.format("Cashier: %s - Identification: %d", name, id);
    }
}
