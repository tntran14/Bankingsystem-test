package com.bankingsystem.service;

import com.bankingsystem.dto.request.CustomerRequest;
import com.bankingsystem.exception.BadRequestException;
import com.bankingsystem.exception.ResourceNotFoundException;
import com.bankingsystem.model.Customer;
import com.bankingsystem.model.CustomerType;
import com.bankingsystem.repository.CustomerRepository;
import com.bankingsystem.repository.CustomerTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CustomerTypeRepository customerTypeRepository;

    @Transactional
    @CacheEvict(value = "customers", allEntries = true)
    public Customer createCustomer(CustomerRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists: " + request.getEmail());
        }

        Customer customer = Customer.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth())
                .build();

        if (request.getCustomerTypeId() != null) {
            CustomerType type = customerTypeRepository.findById(request.getCustomerTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("CustomerType", "id", request.getCustomerTypeId()));
            customer.setCustomerType(type);
        }

        return customerRepository.save(customer);
    }

    @Cacheable(value = "customers", key = "#id")
    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    @Cacheable(value = "customers")
    public Page<Customer> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    public List<Customer> searchCustomersByName(String name) {
        return customerRepository.findByFullNameContainingIgnoreCase(name);
    }

    @Transactional
    @CacheEvict(value = "customers", allEntries = true)
    public Customer updateCustomer(Long id, CustomerRequest request) {
        Customer customer = getCustomerById(id);
        customer.setFullName(request.getFullName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setDateOfBirth(request.getDateOfBirth());

        if (request.getCustomerTypeId() != null) {
            CustomerType type = customerTypeRepository.findById(request.getCustomerTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("CustomerType", "id", request.getCustomerTypeId()));
            customer.setCustomerType(type);
        }

        return customerRepository.save(customer);
    }

    @Transactional
    @CacheEvict(value = "customers", allEntries = true)
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Customer", "id", id);
        }
        customerRepository.deleteById(id);
    }

    /** Thống kê khách hàng theo địa điểm */
    public List<Object[]> getCustomerCountByLocation() {
        return customerRepository.countCustomersByLocation();
    }
}
