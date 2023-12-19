package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AllergeneDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.AllergeneMapper;
import at.ac.tuwien.sepr.groupphase.backend.repository.AllergeneRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.AllergeneService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Service
public class AllergeneServiceImpl implements AllergeneService {
  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final AllergeneRepository allergeneRepository;
  private final AllergeneMapper allergeneMapper;

  public AllergeneServiceImpl(AllergeneRepository allergeneRepository, AllergeneMapper allergeneMapper) {
    this.allergeneRepository = allergeneRepository;
    this.allergeneMapper = allergeneMapper;
  }

  @Override
  public List<AllergeneDto> getAllAllergens() {
    LOGGER.trace("getAllAllergene()");
    return allergeneMapper.allergenesToListAllergeneDtos(allergeneRepository.findAll());
  }
}
