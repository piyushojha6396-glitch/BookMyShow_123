package com.cfs.BMS.service;

import com.cfs.BMS.dto.CityRequest;
import com.cfs.BMS.dto.CityResponse;
import com.cfs.BMS.entity.City;
import com.cfs.BMS.exception.ConflictException;
import com.cfs.BMS.exception.ResourceNotFoundException;
import com.cfs.BMS.mapper.EntityMapper;
import com.cfs.BMS.repository.CityRepository;
import com.cfs.BMS.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;
    private final TheaterRepository theaterRepository;

    @Transactional
    public CityResponse addCity(CityRequest request) {
        String name = request.name().trim();
        if (cityRepository.existsByNameIgnoreCase(name)) {
            throw new ConflictException("City already exists: " + name);
        }
        City city = City.builder().name(name).state(request.state()).build();
        return EntityMapper.toResponse(cityRepository.save(city));
    }

    @Transactional(readOnly = true)
    public List<CityResponse> getAllCities() {
        return cityRepository.findAll(Sort.by("name")).stream().map(EntityMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public City findEntity(Long id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public CityResponse getCityById(Long id) {
        return EntityMapper.toResponse(findEntity(id));
    }

    @Transactional
    public CityResponse updateCity(Long id, CityRequest request) {
        City city = findEntity(id);
        String name = request.name().trim();
        if (cityRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new ConflictException("City already exists: " + name);
        }
        city.setName(name);
        city.setState(request.state());
        return EntityMapper.toResponse(cityRepository.save(city));
    }

    @Transactional
    public void deleteCity(Long id) {
        City city = findEntity(id);
        if (theaterRepository.existsByCityId(id)) {
            throw new ConflictException("City has theaters and cannot be deleted");
        }
        cityRepository.delete(city);
    }
}
