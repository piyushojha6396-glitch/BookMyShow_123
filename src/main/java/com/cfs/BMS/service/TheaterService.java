package com.cfs.BMS.service;

import com.cfs.BMS.dto.PageResponse;
import com.cfs.BMS.dto.TheaterRequest;
import com.cfs.BMS.dto.TheaterResponse;
import com.cfs.BMS.entity.City;
import com.cfs.BMS.entity.Theater;
import com.cfs.BMS.exception.ConflictException;
import com.cfs.BMS.exception.ResourceNotFoundException;
import com.cfs.BMS.mapper.EntityMapper;
import com.cfs.BMS.repository.ScreenRepository;
import com.cfs.BMS.repository.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;
    private final ScreenRepository screenRepository;
    private final CityService cityService;

    @Transactional
    public TheaterResponse addTheater(TheaterRequest request) {
        City city = cityService.findEntity(request.cityId());
        String name = request.name().trim();
        if (theaterRepository.existsByCityIdAndNameIgnoreCase(city.getId(), name)) {
            throw new ConflictException("Theater '" + name + "' already exists in " + city.getName());
        }
        Theater theater = Theater.builder().name(name).address(request.address()).city(city).build();
        return EntityMapper.toResponse(theaterRepository.save(theater));
    }

    @Transactional(readOnly = true)
    public PageResponse<TheaterResponse> getAllTheaters(Pageable pageable) {
        return PageResponse.of(theaterRepository.findAll(pageable), EntityMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Theater findEntity(Long id) {
        return theaterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public TheaterResponse getTheaterById(Long id) {
        return EntityMapper.toResponse(findEntity(id));
    }

    @Transactional(readOnly = true)
    public List<TheaterResponse> getTheaterByCity(Long cityId) {
        cityService.findEntity(cityId);
        return theaterRepository.findByCityIdOrderByName(cityId).stream().map(EntityMapper::toResponse).toList();
    }

    @Transactional
    public TheaterResponse updateTheater(Long id, TheaterRequest request) {
        Theater theater = findEntity(id);
        City city = cityService.findEntity(request.cityId());
        String name = request.name().trim();
        if (theaterRepository.existsByCityIdAndNameIgnoreCaseAndIdNot(city.getId(), name, id)) {
            throw new ConflictException("Theater '" + name + "' already exists in " + city.getName());
        }
        theater.setName(name);
        theater.setAddress(request.address());
        theater.setCity(city);
        return EntityMapper.toResponse(theaterRepository.save(theater));
    }

    @Transactional
    public void deleteTheater(Long id) {
        Theater theater = findEntity(id);
        if (screenRepository.existsByTheaterId(id)) {
            throw new ConflictException("Theater has screens and cannot be deleted");
        }
        theaterRepository.delete(theater);
    }
}
