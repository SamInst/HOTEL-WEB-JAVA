package com.istoe.demo.response;

import com.istoe.demo.enums.RoomStatusEnum;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record RoomsResponse(
        List<Categoria> categoryList

) {
    public record Categoria (
            String category,
            List<Room> rooms
    ){
        public record Room(
                Long id,
                RoomStatusEnum roomStatusEnum,
                Integer roomCapacity,
                Integer singleBedAmount,
                Integer doubleBedAmount,
                Integer bunkbedAmount,
                Integer hammockAmount,
                Holder holder,
                DayUse dayUse
        ){
            public record Holder (
                    Long id,
                    String name,
                    String cpf,
                    String phoneNumber,
                    Integer companionAmount,
                    LocalDate checkin,
                    LocalDate checkout,
                    LocalTime estimatedArrivalTime,
                    LocalTime estimatedDepartureTime
            ){}
            public record DayUse (
                    Long id,
                    LocalDate date,
                    LocalTime start,
                    LocalTime end
            ){}
        }
    }
}
