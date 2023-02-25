package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		customerRepository2.deleteById(customerId);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver>driverList=driverRepository2.findAll();
		Driver wanted=new Driver();
		int max=Integer.MAX_VALUE;
		boolean flag=false;
		for(Driver d:driverList){
			if(d.getDriverId()<max&&d.getCab().getAvailable()){
				max=d.getDriverId();
				flag=true;
				wanted=d;
			}
		}
		if(!flag){
			throw new Exception("No cab available!");
		}
		int perKmRate=wanted.getCab().getPerKmRate();
		int bill=perKmRate*distanceInKm;
		Customer customer=customerRepository2.findById(customerId).get();
		TripBooking tripBooking=new TripBooking();
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setToLocation(toLocation);
		tripBooking.setCustomer(customer);
		tripBooking.setBill(bill);
		tripBooking.setStatus(TripStatus.CONFIRMED);

		List<TripBooking>tripBookingListCustomer=customer.getTripBookingList();
		tripBookingListCustomer.add(tripBooking);

		List<TripBooking>tripBookingListDriver=wanted.getTripBookingList();
		tripBookingListDriver.add(tripBooking);

		customerRepository2.save(customer);
		driverRepository2.save(wanted);
		return tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.COMPLETED);
		tripBookingRepository2.save(tripBooking);
	}
}
