package br.gov.es.participe.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.gov.es.participe.controller.dto.controlPanel.ControlPanelDto;
import br.gov.es.participe.controller.dto.controlPanel.HeatMapChartDto;
import br.gov.es.participe.controller.dto.LocalityTypeDto;
import br.gov.es.participe.controller.dto.controlPanel.ControlPanelChartDto;
import br.gov.es.participe.controller.dto.controlPanel.MicroregionChartQueryDto;
import br.gov.es.participe.enumerator.ResultTypeControlPanelEnum;
import br.gov.es.participe.repository.AttendRepository;
import br.gov.es.participe.repository.ControlPanelRepository;

@Service
public class ControlPanelService {

    @Autowired
    private ControlPanelRepository repository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private HighlightService highlightService;

    @Autowired
    private AttendRepository attendRepository;

    @Autowired
    private LocalityService localityService;

    @Autowired
    private LocalityTypeService localityTypeService;

    public ControlPanelDto getInformationsDashboard(Long idConference, ResultTypeControlPanelEnum result, String origin,
            List<Long> meetings, Long microregionChartAgroup, Long microregionLocalitySelected,
            Long structureItemSelected, Long structureItemPlanSelected, Boolean stLastLevelLocality,
            Boolean stLastLevelPlanItem) {
        ControlPanelDto dto = new ControlPanelDto();
        dto.setParticipants(attendRepository.countParticipationByConference(idConference));
        dto.setCounties(localityService.countLocalitiesParticipation(idConference));
        dto.setHighlights(highlightService.countHighlightByConference(idConference));
        dto.setProposals(commentService.countCommentByConference(idConference));

        origin = returnOriginConverted(origin);

        List<MicroregionChartQueryDto> microregionChartQuery = returnData(idConference, result, microregionChartAgroup,
                microregionLocalitySelected, structureItemSelected, structureItemPlanSelected, stLastLevelLocality,
                origin, meetings, stLastLevelPlanItem);

        dto.setMicroregionChart(returnMicroregionChartDto(result, microregionChartQuery));
        dto.setStrategicAreaChart(returnPlanItensChartDto(result, microregionChartQuery, idConference,
                microregionChartAgroup, microregionLocalitySelected, structureItemSelected, structureItemPlanSelected,
                stLastLevelLocality, origin, meetings, stLastLevelPlanItem));
        dto.setHeatMapChart(returnHeatMapDto(result, microregionChartQuery));
        return dto;
    }

    private String returnOriginConverted(String origin) {
        if (origin == null || origin.isEmpty())
            return origin;

        if (origin.equals("PRESENTIAL"))
            return "pre";

        if (origin.equals("REMOTE"))
            return "com";

        throw new IllegalArgumentException("Invalid origin.");
    }

    public List<LocalityTypeDto> getAllTypeLocality(Long idDomain, Long idTypeLocality) {
        List<LocalityTypeDto> dto = repository.findDataTypeLocality(idDomain, idTypeLocality);
        Collections.reverse(dto);
        dto.add(new LocalityTypeDto(localityTypeService.find(idTypeLocality)));
        return dto;
    }

    private List<ControlPanelChartDto> returnMicroregionChartDto(ResultTypeControlPanelEnum result,
            List<MicroregionChartQueryDto> microregionChartQuery) {

        List<ControlPanelChartDto> microregionChartDto = new ArrayList<>();

        if (ResultTypeControlPanelEnum.HIGHLIGHTS.equals(result)) {
            Map<Long, Long> likesPerType = microregionChartQuery.stream()
                    .collect(Collectors.groupingBy(MicroregionChartQueryDto::getId,
                            Collectors.summingLong(MicroregionChartQueryDto::getQuantityHighlight)));

            for (Long chave : likesPerType.keySet()) {
                ControlPanelChartDto microDto = new ControlPanelChartDto();
                String nameRegion = microregionChartQuery.stream().filter(filter -> filter.getId().equals(chave))
                        .map(map -> map.getName()).findFirst().orElse("Without Name");
                microDto.setId(chave);
                microDto.setDescription(nameRegion);
                microDto.setQuantity(likesPerType.get(chave).longValue());
                microregionChartDto.add(microDto);
            }
        }

        if (ResultTypeControlPanelEnum.PROPOSALS.equals(result)) {
            Map<Long, Long> likesPerType = microregionChartQuery.stream()
                    .collect(Collectors.groupingBy(MicroregionChartQueryDto::getId,
                            Collectors.summingLong(MicroregionChartQueryDto::getQuantityComment)));

            for (Long chave : likesPerType.keySet()) {
                ControlPanelChartDto microDto = new ControlPanelChartDto();
                String nameRegion = microregionChartQuery.stream().filter(filter -> filter.getId().equals(chave))
                        .map(map -> map.getName()).findFirst().orElse("Without Name");
                microDto.setId(chave);
                microDto.setDescription(nameRegion);
                microDto.setQuantity(likesPerType.get(chave).longValue());
                microregionChartDto.add(microDto);
            }
        }

        if (ResultTypeControlPanelEnum.PARTICIPANTS.equals(result)) {
            Map<Long, Long> likesPerType = microregionChartQuery.stream()
                    .collect(Collectors.groupingBy(MicroregionChartQueryDto::getId,
                            Collectors.summingLong(MicroregionChartQueryDto::getQuantityParticipation)));

            for (Long chave : likesPerType.keySet()) {
                ControlPanelChartDto microDto = new ControlPanelChartDto();
                String nameRegion = microregionChartQuery.stream().filter(filter -> filter.getId().equals(chave))
                        .map(map -> map.getName()).findFirst().orElse("Without Name");
                microDto.setId(chave);
                microDto.setDescription(nameRegion);
                microDto.setQuantity(likesPerType.get(chave).longValue());
                microregionChartDto.add(microDto);
            }
        }
        return microregionChartDto;

    }

    private List<HeatMapChartDto> returnHeatMapDto(ResultTypeControlPanelEnum result,
            List<MicroregionChartQueryDto> microregionChartQuery) {

        List<HeatMapChartDto> microregionChartDto = new ArrayList<>();

        if (ResultTypeControlPanelEnum.HIGHLIGHTS.equals(result)) {
            Map<Long, Long> likesPerType = microregionChartQuery.stream()
                    .collect(Collectors.groupingBy(MicroregionChartQueryDto::getId,
                            Collectors.summingLong(MicroregionChartQueryDto::getQuantityHighlight)));

            for (Long chave : likesPerType.keySet()) {
                MicroregionChartQueryDto dto = microregionChartQuery.stream()
                        .filter(filter -> filter.getId().equals(chave)).findFirst().orElse(null);
                if (dto == null)
                    continue;
                HeatMapChartDto heatMapChartDto = buildHeatMapChartDto(dto, chave, likesPerType.get(chave).longValue());
                if (heatMapChartDto == null)
                    continue;
                microregionChartDto.add(heatMapChartDto);
            }
        }

        if (ResultTypeControlPanelEnum.PROPOSALS.equals(result)) {
            Map<Long, Long> likesPerType = microregionChartQuery.stream()
                    .collect(Collectors.groupingBy(MicroregionChartQueryDto::getId,
                            Collectors.summingLong(MicroregionChartQueryDto::getQuantityComment)));

            for (Long chave : likesPerType.keySet()) {
                MicroregionChartQueryDto dto = microregionChartQuery.stream()
                        .filter(filter -> filter.getId().equals(chave)).findFirst().orElse(null);
                if (dto == null)
                    continue;

                HeatMapChartDto heatMapChartDto = buildHeatMapChartDto(dto, chave, likesPerType.get(chave).longValue());
                if (heatMapChartDto == null)
                    continue;
                microregionChartDto.add(heatMapChartDto);
            }
        }

        if (ResultTypeControlPanelEnum.PARTICIPANTS.equals(result)) {
            Map<Long, Long> likesPerType = microregionChartQuery.stream()
                    .collect(Collectors.groupingBy(MicroregionChartQueryDto::getId,
                            Collectors.summingLong(MicroregionChartQueryDto::getQuantityParticipation)));

            for (Long chave : likesPerType.keySet()) {
                MicroregionChartQueryDto dto = microregionChartQuery.stream()
                        .filter(filter -> filter.getId().equals(chave)).findFirst().orElse(null);
                if (dto == null)
                    continue;
                HeatMapChartDto heatMapChartDto = buildHeatMapChartDto(dto, chave, likesPerType.get(chave).longValue());
                if (heatMapChartDto == null)
                    continue;
                microregionChartDto.add(heatMapChartDto);
            }
        }
        return microregionChartDto;
    }

    private HeatMapChartDto buildHeatMapChartDto(MicroregionChartQueryDto dto, Long chave, Long count) {
        String latitudeLongitude[] = (dto.getLatitudeLongitude() == null || dto.getLatitudeLongitude().isEmpty())
                ? new String[0]
                : dto.getLatitudeLongitude().split(",");
        if (latitudeLongitude.length <= 0)
            return null;

        String latitude;
        String longitude;

        BigDecimal latitudeConvertido = BigDecimal.ZERO;
        BigDecimal longitudeConvertido = BigDecimal.ZERO;

        latitude = (latitudeLongitude.length == 2) ? latitudeLongitude[0] : "0";
        longitude = (latitudeLongitude.length == 2) ? latitudeLongitude[1] : "0";

        latitudeConvertido = new BigDecimal(latitude.trim());
        longitudeConvertido = new BigDecimal(longitude.trim());

        return new HeatMapChartDto(latitudeConvertido, longitudeConvertido, count);
    }

    private List<ControlPanelChartDto> returnPlanItensChartDto(ResultTypeControlPanelEnum result,
            List<MicroregionChartQueryDto> microregionChartQuery, Long idConference, Long microregionChartAgroup,
            Long microregionLocalitySelected, Long structureItemSelected, Long structureItemPlanSelected,
            Boolean stLastLevelLocality, String origin, List<Long> meetings, Boolean stLastLevelPlanItem) {

        List<ControlPanelChartDto> microregionChartDto = new ArrayList<>();

        if (ResultTypeControlPanelEnum.HIGHLIGHTS.equals(result)) {
            Map<Long, Long> likesPerType = microregionChartQuery.stream()
                    .collect(Collectors.groupingBy(MicroregionChartQueryDto::getIdPlanItem,
                            Collectors.summingLong(MicroregionChartQueryDto::getQuantityHighlight)));

            for (Long chave : likesPerType.keySet()) {
                ControlPanelChartDto microDto = new ControlPanelChartDto();
                String name = microregionChartQuery.stream().filter(filter -> filter.getIdPlanItem().equals(chave))
                        .map(map -> map.getPlanItemName()).findFirst().orElse("Without Name");
                microDto.setId(chave);
                microDto.setDescription(name);
                microDto.setQuantity(likesPerType.get(chave).longValue());
                microregionChartDto.add(microDto);
            }
        }

        if (ResultTypeControlPanelEnum.PROPOSALS.equals(result)) {
            Map<Long, Long> likesPerType = microregionChartQuery.stream()
                    .collect(Collectors.groupingBy(MicroregionChartQueryDto::getIdPlanItem,
                            Collectors.summingLong(MicroregionChartQueryDto::getQuantityComment)));

            for (Long chave : likesPerType.keySet()) {
                ControlPanelChartDto microDto = new ControlPanelChartDto();
                String name = microregionChartQuery.stream().filter(filter -> filter.getIdPlanItem().equals(chave))
                        .map(map -> map.getPlanItemName()).findFirst().orElse("Without Name");
                microDto.setId(chave);
                microDto.setDescription(name);
                microDto.setQuantity(likesPerType.get(chave).longValue());
                microregionChartDto.add(microDto);
            }
        }

        if (ResultTypeControlPanelEnum.PARTICIPANTS.equals(result)) {
            List<MicroregionChartQueryDto> microregionChartQueryParticipant = new ArrayList<>();

            microregionChartQueryParticipant = returnData(idConference, ResultTypeControlPanelEnum.HIGHLIGHTS,
                    microregionChartAgroup, microregionLocalitySelected, structureItemSelected,
                    structureItemPlanSelected, stLastLevelLocality, origin, meetings, stLastLevelPlanItem);

            Map<Long, Long> highlights = microregionChartQueryParticipant.stream()
                    .collect(Collectors.groupingBy(MicroregionChartQueryDto::getIdPlanItem,
                            Collectors.summingLong(MicroregionChartQueryDto::getQuantityComment)));

            Map<Long, Long> comments = microregionChartQueryParticipant.stream()
                    .collect(Collectors.groupingBy(MicroregionChartQueryDto::getIdPlanItem,
                            Collectors.summingLong(MicroregionChartQueryDto::getQuantityHighlight)));

            for (Long chave : highlights.keySet()) {
                ControlPanelChartDto microDto = new ControlPanelChartDto();
                String name = microregionChartQueryParticipant.stream()
                        .filter(filter -> filter.getIdPlanItem().equals(chave)).map(map -> map.getPlanItemName())
                        .findFirst().orElse("Without Name");
                Long commentQuantity = comments.get(chave).longValue();
                Long highlightQuantity = highlights.get(chave).longValue();
                microDto.setId(chave);
                microDto.setDescription(name);
                microDto.setQuantity((commentQuantity + highlightQuantity));
                microregionChartDto.add(microDto);
            }
        }
        return microregionChartDto;
    }

    private List<MicroregionChartQueryDto> returnData(Long idConference, ResultTypeControlPanelEnum result,
            Long microregionChartAgroup, Long microregionLocalitySelected, Long structureItemSelected,
            Long structureItemPlanSelected, Boolean stLastLevelLocality, String origin, List<Long> meetings,
            Boolean stLastLevelPlanItem) {

        List<MicroregionChartQueryDto> resultadoQuery = returnDataByCenario(idConference, result,
                microregionChartAgroup, microregionLocalitySelected, structureItemSelected, structureItemPlanSelected,
                stLastLevelLocality, origin, meetings, stLastLevelPlanItem);

        resultadoQuery.forEach(row -> {
            if (row.getQuantityComment() == null)
                row.setQuantityComment(0L);
            if (row.getQuantityParticipation() == null)
                row.setQuantityParticipation(0L);
            if (row.getQuantityHighlight() == null)
                row.setQuantityHighlight(0L);
        });

        return resultadoQuery;
    }

    private List<MicroregionChartQueryDto> returnDataByCenario(Long idConference, ResultTypeControlPanelEnum result,
            Long microregionChartAgroup, Long microregionLocalitySelected, Long structureItemSelected,
            Long structureItemPlanSelected, Boolean stLastLevelLocality, String origin, List<Long> meetings,
            Boolean stLastLevelPlanItem) {
        if (ResultTypeControlPanelEnum.PARTICIPANTS.equals(result)) {
            if (microregionLocalitySelected == null)
                return repository.findDataMicroregionMapDashboardFromIdConferenceParticipationAgroup(idConference,
                        microregionChartAgroup, meetings);

            return repository.findDataMicroregionMapDashboardFromIdConferenceParticipation(idConference,
                    microregionLocalitySelected, meetings);
        }

        if (microregionLocalitySelected == null && structureItemPlanSelected == null)
            return repository.findDataCommentHighlightWitoutFilter(idConference, microregionChartAgroup,
                    structureItemSelected, origin, meetings);

        if (microregionLocalitySelected == null && structureItemPlanSelected != null && !stLastLevelPlanItem)
            return repository.findDataCommentHighlightStructureItemPlanSelected(idConference, microregionChartAgroup,
                    structureItemPlanSelected, origin, meetings);

        if (microregionLocalitySelected == null && structureItemPlanSelected != null && stLastLevelPlanItem)
            return repository.findDataCommentHighlightStructureItemPlanSelectedLastLevel(idConference,
                    microregionChartAgroup, structureItemPlanSelected, origin, meetings);

        if (microregionLocalitySelected != null && structureItemPlanSelected == null && stLastLevelLocality)
            return repository.findDataCommentHighlightLocalitySelectedLastLevel(idConference,
                    microregionLocalitySelected, structureItemSelected, origin, meetings);

        if (microregionLocalitySelected != null && structureItemPlanSelected == null)
            return repository.findDataCommentHighlightLocalitySelected(idConference, microregionLocalitySelected,
                    structureItemSelected, origin, meetings);

        if (microregionLocalitySelected != null && structureItemPlanSelected != null && stLastLevelLocality)
            return repository.findDataCommentHighlightAllFilterLastLevel(idConference, microregionLocalitySelected,
                    structureItemPlanSelected, origin, meetings);

        return repository.findDataCommentHighlightAllFilter(idConference, microregionLocalitySelected,
                structureItemPlanSelected, origin, meetings);
    }

}
