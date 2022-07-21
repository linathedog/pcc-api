package gr.grnet.pccapi.service;

import gr.grnet.pccapi.dto.PrefixDto;
import gr.grnet.pccapi.dto.PrefixResponseDto;
import gr.grnet.pccapi.entity.Domain;
import gr.grnet.pccapi.entity.Prefix;
import gr.grnet.pccapi.entity.Provider;
import gr.grnet.pccapi.entity.Service;
import gr.grnet.pccapi.exception.ConflictException;
import gr.grnet.pccapi.mapper.PrefixMapper;
import gr.grnet.pccapi.repository.DomainRepository;
import gr.grnet.pccapi.repository.PrefixRepository;
import gr.grnet.pccapi.repository.ProviderRepository;
import gr.grnet.pccapi.repository.ServiceRepository;
import lombok.AllArgsConstructor;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.ws.rs.NotFoundException;

@ApplicationScoped
@AllArgsConstructor
public class PrefixService {

     DomainRepository domainRepository;
     ProviderRepository providerRepository;
     ServiceRepository serviceRepository;
     PrefixRepository prefixRepository;
     Logger log;

     /**
      * Creates a new prefix based on the provided arguments,
      * runs validation checks and returns the appropriate response dto
      */
     @Transactional
    public PrefixResponseDto create(PrefixDto prefixDto) {

         log.info("Inserting new prefix . . .");


         // check the uniqueness of the provided name
         if (prefixRepository.existsByName(prefixDto.getName())) {
             throw new ConflictException("Prefix name already exists");
         }

        // check the existence of the provided service
        Service service = serviceRepository.findByIdOptional(prefixDto.getServiceId())
                .orElseThrow(() -> new NotFoundException("Service not found"));

        // check the existence of the provided domain
        Domain domain = domainRepository.findByIdOptional(prefixDto.getDomainId())
                .orElseThrow(() -> new NotFoundException("Domain not found"));

        // check the existence of the provided provider
        Provider provider = providerRepository.findByIdOptional(prefixDto.getProviderId())
                .orElseThrow(() -> new NotFoundException("Provider not found"));

        Prefix prefix = new Prefix()
                .setService(service)
                .setDomain(domain)
                .setProvider(provider)
                .setOwner(prefixDto.getOwner())
                .setName(prefixDto.getName())
                .setUsedBy(prefixDto.getUsedBy())
                .setStatus(prefixDto.getStatus());

        prefixRepository.persist(prefix);

       return PrefixMapper.INSTANCE.prefixToResponseDto(prefix);
    }

    /**
     * Returns a Prefix by the given ID
     * @return The stored Prefix has been turned into a response body.
     */
    public PrefixResponseDto getById(Integer id){

        log.infof("Fetching the Prefix with ID : %s", id);

        var prefix = prefixRepository.findByIdOptional(id)
                .orElseThrow(() -> new NotFoundException("Prefix not found"));

        return PrefixMapper.INSTANCE.prefixToResponseDto(prefix);
    }

    /**
     * This method delegates a prefix deletion query to {@link PrefixRepository prefixRepository}.
     *
     * @param id The prefix ID to be deleted
     */
    @Transactional
    public void delete(Integer id){
         boolean deleted = prefixRepository.deleteById(id);
         if (!deleted) {
             throw new NotFoundException("Prefix not found");
         }
    }
}
