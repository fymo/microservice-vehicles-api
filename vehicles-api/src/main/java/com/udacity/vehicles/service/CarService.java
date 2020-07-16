package com.udacity.vehicles.service;

import com.udacity.vehicles.client.maps.Address;
import com.udacity.vehicles.client.prices.Price;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
public class CarService {

    private final CarRepository repository;

    private WebClient maps;
    private WebClient pricing;

    private static final String[] CURRENCIES = {"USD", "EUR", "GBP", "CAD", "EGP"};

    public CarService(CarRepository repository, WebClient maps, WebClient pricing) {
        this.repository = repository;
        this.maps = maps;
        this.pricing = pricing;
    }


    public List<Car> list() {

        return repository.findAll().stream().map(car -> {
            return loadPriceAndLocation(car);
        }).collect(Collectors.toList());
    }

    public Car findById(Long id) {
        Car car = repository.findById(id).orElseThrow(CarNotFoundException::new);
        return loadPriceAndLocation(car);
    }

    public Car save(Car car) {

        if (car.getId() != null) {
            return repository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        Price price = car.getPrice();
                        pricing.put().uri("/prices/{id}", car.getId()).body(Mono.just(price), Price.class).retrieve().bodyToMono(Price.class).block();

                        carToBeUpdated.setPrice(price);
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());

                        return repository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }
        Car savedCar = repository.save(car);

        Price price = new Price();
        price.setVehicleId(savedCar.getId());
        ThreadLocalRandom generator = ThreadLocalRandom.current();

        int randomIndex = generator.nextInt(CURRENCIES.length);

        String currency = CURRENCIES[randomIndex];
        price.setCurrency(currency);

        price.setPrice(new BigDecimal(generator.nextDouble(10000.0, 1000000.0)));
        pricing.post().uri("/prices").body(Mono.just(price), Price.class).retrieve().bodyToMono(Price.class).block();

        return savedCar;
    }

    public void delete(Long id) {
        System.out.println("Delete : " + id);
        Car car = repository.findById(id).orElseThrow(CarNotFoundException::new);

        System.out.println("Car : " + car);

        pricing.delete().uri("/prices/{id}", id).retrieve().bodyToMono(Void.class).block();

        repository.delete(car);
    }

    private Car loadPriceAndLocation(Car car){
        Price price = pricing.get().uri("/prices/{id}", car.getId())
                .retrieve().bodyToMono(Price.class).block();

        car.setPrice(price);

        Location location = car.getLocation();

        Address address = maps.get().uri("/maps?lat={lat}&lon={long}", location.getLat(), location.getLon())
                .retrieve().bodyToMono(Address.class).block();

        location.setAddress(address.getAddress());
        location.setCity(address.getCity());
        location.setState(address.getState());
        location.setZip(address.getZip());

        car.setLocation(location);

        return car;
    }
}
