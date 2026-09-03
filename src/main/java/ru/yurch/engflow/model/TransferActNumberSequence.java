package ru.yurch.engflow.model;

import jakarta.persistence.*;

@Entity @Table(name = "transfer_act_number_sequences")
public class TransferActNumberSequence {
    @Id @Column(name="sequence_year") private Integer year;
    @Column(name = "next_number", nullable = false) private Integer nextNumber;
    public Integer getYear(){return year;} public void setYear(Integer year){this.year=year;}
    public Integer getNextNumber(){return nextNumber;} public void setNextNumber(Integer nextNumber){this.nextNumber=nextNumber;}
}
