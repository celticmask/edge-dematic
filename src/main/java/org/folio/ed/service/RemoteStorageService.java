package org.folio.ed.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.folio.ed.client.RemoteStorageClient;
import org.folio.ed.domain.dto.AccessionQueueRecord;
import org.folio.ed.domain.dto.Configuration;
import org.folio.ed.domain.dto.RetrievalQueueRecord;
import org.folio.ed.domain.request.ItemBarcodeRequest;
import org.folio.ed.domain.dto.AsrItem;
import org.folio.ed.domain.dto.AsrItems;
import org.folio.ed.domain.dto.AsrRequest;
import org.folio.ed.domain.dto.AsrRequests;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RemoteStorageService {
  private static final String STAGING_DIRECTOR_NAME = "Dematic_SD";

  private final Map<String, List<RetrievalQueueRecord>> retrievalsMap = new HashMap<>();

  private final RemoteStorageClient remoteStorageClient;

  public List<AccessionQueueRecord> getAccessionQueueRecords(String storageId) {
    return remoteStorageClient.getAccessionsByQuery(buildQueryByStorageId(storageId))
      .getResult();
  }

  public List<RetrievalQueueRecord> getRetrievalQueueRecords(String storageId) {
    retrievalsMap.put(storageId, remoteStorageClient.getRetrievalsByQuery(buildQueryByStorageId(storageId)).getResult());
    return retrievalsMap.get(storageId);
  }

  public RetrievalQueueRecord getRetrievalByBarcode(String barcode, String configId) {
    return retrievalsMap.getOrDefault(configId, Collections.emptyList()).stream()
      .filter(record -> barcode.equals(record.getItemBarcode()))
      .findAny().orElse(null);
  }

  public List<Configuration> getStagingDirectorConfigurations() {
    return remoteStorageClient.getStorageConfigurations()
      .getConfigurations()
      .stream()
      .filter(configuration -> STAGING_DIRECTOR_NAME.equals(configuration.getProviderName()))
      .collect(Collectors.toList());
  }

  public AsrItems getAsrItems(String storageId) {
    var asrItems = new AsrItems();
    asrItems.asrItems(remoteStorageClient.getAccessionsByQuery(buildQueryByStorageId(storageId))
      .getResult()
      .stream()
      .map(this::mapToAsrItem)
      .collect(Collectors.toList()));
    return asrItems;
  }

  @Async
  public ResponseEntity<String> setAccessionedAsync(String itemBarcode) {
    return remoteStorageClient.setAccessionedByBarcode(itemBarcode);
  }

  @Async
  public ResponseEntity<String> setRetrievedAsync(String itemBarcode) {
    return remoteStorageClient.setRetrievalByBarcode(itemBarcode);
  }

  public ResponseEntity<String> checkInItemByBarcode(String remoteStorageConfigurationId, String itemBarcode) {
    var itemBarcodeRequest = new ItemBarcodeRequest();
    itemBarcodeRequest.setItemBarcode(itemBarcode);
    return remoteStorageClient.checkInItem(remoteStorageConfigurationId, itemBarcodeRequest);
  }

  public ResponseEntity<String> returnItemByBarcode(String remoteStorageConfigurationId, String itemBarcode) {
    var itemBarcodeRequest = new ItemBarcodeRequest();
    itemBarcodeRequest.setItemBarcode(itemBarcode);
    return remoteStorageClient.returnItem(remoteStorageConfigurationId, itemBarcodeRequest);
  }

  public AsrRequests getRequests(String remoteStorageConfigurationId) {
    var asrRequests = new AsrRequests();
    asrRequests.asrRequests(remoteStorageClient.getRetrievalsByQuery(buildQueryByStorageId(remoteStorageConfigurationId))
      .getResult()
      .stream()
      .map(this::mapToAsrRequest)
      .collect(Collectors.toList()));
    return asrRequests;
  }

  private String buildQueryByStorageId(String storageId) {
    return "storageId=" + storageId;
  }

  private AsrItem mapToAsrItem(AccessionQueueRecord accessionQueueRecord) {
    AsrItem asrItem = new AsrItem();
    asrItem.setTitle(accessionQueueRecord.getInstanceTitle());
    asrItem.setAuthor(accessionQueueRecord.getInstanceAuthor());
    asrItem.setItemNumber(accessionQueueRecord.getItemBarcode());
    asrItem.setCallNumber(accessionQueueRecord.getCallNumber());
    return asrItem;
  }

  private AsrRequest mapToAsrRequest(RetrievalQueueRecord retrievalQueueRecord) {
    var asrRequest = new AsrRequest();
    asrRequest.setHoldId(retrievalQueueRecord.getHoldId());
    asrRequest.setItemBarcode(retrievalQueueRecord.getItemBarcode());
    asrRequest.setTitle(retrievalQueueRecord.getInstanceTitle());
    asrRequest.setAuthor(retrievalQueueRecord.getInstanceAuthor());
    asrRequest.setCallNumber(retrievalQueueRecord.getCallNumber());
    asrRequest.setPatronBarcode(retrievalQueueRecord.getPatronBarcode());
    asrRequest.setPatronName(retrievalQueueRecord.getPatronName());
    asrRequest.setRequestDate(convertToDate(retrievalQueueRecord.getRetrievedDateTime()));
    asrRequest.setPickupLocation(retrievalQueueRecord.getPickupLocation());
    asrRequest.setRequestStatus(retrievalQueueRecord.getRequestStatus());
    asrRequest.setRequestNote(retrievalQueueRecord.getRequestNote());
    return asrRequest;
  }

  public Date convertToDate(LocalDateTime dateToConvert) {
    if (Objects.nonNull(dateToConvert)) {
      return Timestamp.valueOf(dateToConvert);
    } else {
      return null;
    }

  }

}
