package com.algaworks.algasensors.device.management.api.controller;


import com.algaworks.algasensors.device.management.api.model.SensorInput;
import com.algaworks.algasensors.device.management.api.model.SensorOutput;
import com.algaworks.algasensors.device.management.common.IdGenerator;
import com.algaworks.algasensors.device.management.domain.model.Sensor;
import com.algaworks.algasensors.device.management.domain.model.SensorId;
import com.algaworks.algasensors.device.management.domain.repository.SensorRepository;
import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {


    private final SensorRepository sensorRepository;

    @GetMapping
    public Page<SensorOutput> search(@PageableDefault Pageable pageable) {
        Page<Sensor> sensors = sensorRepository.findAll(pageable);
        return sensors.map(this::convertToModel);
    }

    @GetMapping("{sensorId}")
    public SensorOutput getOne(@PathVariable TSID sensorId) {
        Sensor sensor = sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return convertToModel(sensor);
    }


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorOutput create (@RequestBody SensorInput input) {
        Sensor sensor = Sensor.builder()
                .id(new SensorId(IdGenerator.generateTSID()))
                .name(input.getName())
                .ip(input.getIp())
                .location(input.getLocation())
                .protocol(input.getProtocol())
                .model(input.getModel())
                .enabled(false)
                .build();

        sensor = sensorRepository.saveAndFlush(sensor);

        return convertToModel(sensor);
    }

    @PutMapping("{sensorId}")
    @ResponseStatus(HttpStatus.OK)
    public SensorOutput update (@PathVariable TSID sensorId ,@RequestBody SensorInput input) {
        Sensor actualSensor = sensorRepository.findById(new SensorId(sensorId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        actualSensor.setName(input.getName());
        actualSensor.setIp(input.getIp());
        actualSensor.setLocation(input.getLocation());
        actualSensor.setProtocol(input.getProtocol());
        actualSensor.setModel(input.getModel());
        actualSensor.setEnabled(false);

        actualSensor = sensorRepository.saveAndFlush(actualSensor);

        return convertToModel(actualSensor);
    }

    @DeleteMapping("{sensorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable TSID sensorId) {
        Sensor sensor = sensorRepository.findById(new SensorId(sensorId)).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        sensorRepository.delete(sensor);
    }

    @PutMapping("{sensorId}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable (@PathVariable TSID sensorId) {
        Sensor sensor = sensorRepository.findById(new SensorId(sensorId)).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        sensor.setEnabled(true);
        sensorRepository.save(sensor);
    }

    @PutMapping("{sensorId}/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable (@PathVariable TSID sensorId) {
        Sensor sensor = sensorRepository.findById(new SensorId(sensorId)).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        sensor.setEnabled(false);
        sensorRepository.save(sensor);
    }



    private SensorOutput convertToModel(Sensor sensor) {
        return SensorOutput.builder()
                .id(sensor.getId().getValue())
                .name(sensor.getName())
                .ip(sensor.getIp())
                .location(sensor.getLocation())
                .protocol(sensor.getProtocol())
                .model(sensor.getModel())
                .enable(sensor.getEnabled())
                .build();
    }


}
