package Filters;

public interface Filter<Item> {
    boolean check(Item item);
}
