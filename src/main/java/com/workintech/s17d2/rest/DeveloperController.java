package com.workintech.s17d2.rest;

import jakarta.annotation.PostConstruct;
import com.workintech.s17d2.model.Developer;
import com.workintech.s17d2.model.Experience;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.workintech.s17d2.tax.Taxable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/developers")
public class DeveloperController {

    public Map<Integer, Developer> developers;
    private Taxable taxable;

    @Autowired
    public DeveloperController(Taxable taxable) {
        this.taxable = taxable;
    }

    @PostConstruct
    public void init() {
        this.developers = new HashMap<>();
        this.developers.put(1, new Developer(1, "Kemal", 60000, Experience.JUNIOR));
    }

    @GetMapping
    public List<Developer> getDeveloper() {
        return new ArrayList<>(developers.values());
    }


    @GetMapping("/{id}")
    public Developer getById(@PathVariable("id") int id) {
        return developers.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Developer postDeveloper(@RequestBody Developer developer) {
        double netSalary = calculateSalaryAfterTax(developer.getSalary(), developer.getExperience());
        developer.setSalary(netSalary);

        this.developers.put(developer.getId(), developer);
        return developer;
    }

    @PutMapping("/{id}")
    public Developer update(@PathVariable("id") int id, @RequestBody Developer updated) {
        updated.setId(id);

        double netSalary = calculateSalaryAfterTax(updated.getSalary(), updated.getExperience());
        updated.setSalary(netSalary);

        Developer oldDeveloper = this.developers.replace(id, updated);
        if (oldDeveloper == null) {
            return null;
        }

        return updated;
    }

    @DeleteMapping("/{id}")
    public Developer delete(@PathVariable("id") int id) {
        if (developers.containsKey(id)) {
            return this.developers.remove(id);
        } else {
            return null;
        }
    }


    private double calculateSalaryAfterTax(double salary, Experience experience) {
        if (experience == Experience.JUNIOR) {
            return salary - (salary * (taxable.getSimpleTaxRate() / 100));
        } else if (experience == Experience.MID) {
            return salary - (salary * (taxable.getMiddleTaxRate() / 100));
        } else if (experience == Experience.SENIOR) {
            return salary - (salary * (taxable.getUpperTaxRate() / 100));
        }
        return salary;
    }
}