package ru.yurch.engflow.service;

public class DuplicateProjectDesignationException extends RuntimeException {

    public DuplicateProjectDesignationException(String designation) {
        super("Проект с обозначением «" + designation + "» уже существует");
    }
}
