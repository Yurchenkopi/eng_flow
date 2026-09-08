package ru.yurch.engflow.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.yurch.engflow.model.*;
import ru.yurch.engflow.repository.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

@Service @Transactional(readOnly=true)
public class SupplierInvoiceService {
    private static final Set<String> ALLOWED_EXTENSIONS=Set.of("pdf","png","jpg","jpeg","webp","xls","xlsx");
    private final SupplierInvoiceRepository invoices;private final SupplierInvoiceLineRepository lines;private final ProcurementLineRepository procurementLines;private final ProcurementService procurements;private final Path storageRoot;
    public SupplierInvoiceService(SupplierInvoiceRepository invoices,SupplierInvoiceLineRepository lines,ProcurementLineRepository procurementLines,ProcurementService procurements,@Value("${engflow.invoice-storage:./data/invoices}") String storage){this.invoices=invoices;this.lines=lines;this.procurementLines=procurementLines;this.procurements=procurements;this.storageRoot=Path.of(storage).toAbsolutePath().normalize();}
    public SupplierInvoice find(Long projectId,Long procurementId,Long id){procurements.find(projectId,procurementId);return invoices.findByIdAndProcurementId(id,procurementId).orElseThrow(()->new IllegalArgumentException("Счет не найден"));}
    public List<SupplierInvoiceLine> lines(Long invoiceId){return lines.findBySupplierInvoiceIdOrderByIdAsc(invoiceId);}
    @Transactional public SupplierInvoice create(Long projectId,Long procurementId,SupplierInvoice value,MultipartFile file){value.setId(null);value.setProcurement(procurements.find(projectId,procurementId));normalize(value);storeFile(value,file);return invoices.save(value);}
    @Transactional public SupplierInvoice update(Long projectId,Long procurementId,Long id,SupplierInvoice value,MultipartFile file){SupplierInvoice current=find(projectId,procurementId,id);current.setInvoiceNumber(value.getInvoiceNumber());current.setInvoiceDate(value.getInvoiceDate());current.setPaymentSubmittedDate(value.getPaymentSubmittedDate());current.setNotes(trim(value.getNotes()));normalize(current);if(file!=null&&!file.isEmpty()){String previousStorageName=current.getFileStorageName();storeFile(current,file);deleteStoredFile(previousStorageName);}return invoices.save(current);}
    @Transactional public void submitForPayment(Long projectId,Long procurementId,Long id){find(projectId,procurementId,id).setPaymentSubmittedDate(LocalDate.now());}
    @Transactional public SupplierInvoiceLine addLine(Long projectId,Long procurementId,Long invoiceId,SupplierInvoiceLine value){SupplierInvoice invoice=find(projectId,procurementId,invoiceId);if(value.getProcurementLine()==null||value.getProcurementLine().getId()==null)throw new IllegalArgumentException("Выберите позицию закупки");ProcurementLine procurementLine=procurementLines.findByIdAndProcurementId(value.getProcurementLine().getId(),procurementId).orElseThrow(()->new IllegalArgumentException("Позиция закупки не принадлежит этой закупке"));positive(value.getQuantity());if(lines.existsBySupplierInvoiceIdAndProcurementLineId(invoiceId,procurementLine.getId()))throw new IllegalArgumentException("Позиция уже добавлена в этот счет");BigDecimal total=lines.allocatedByProcurementLine(procurementLine.getId()).add(value.getQuantity());if(total.compareTo(procurementLine.getRequestedQuantity())>0)throw new IllegalArgumentException("Суммарное количество по счетам превышает запрошенное");value.setId(null);value.setSupplierInvoice(invoice);value.setProcurementLine(procurementLine);value.setNotes(trim(value.getNotes()));return lines.save(value);}
    @Transactional public void deleteFile(Long projectId,Long procurementId,Long id){SupplierInvoice invoice=find(projectId,procurementId,id);deleteStoredFile(invoice);invoice.setFileOriginalName(null);invoice.setFileStorageName(null);invoice.setFileContentType(null);}
    public Resource file(Long projectId,Long procurementId,Long id){SupplierInvoice invoice=find(projectId,procurementId,id);if(!invoice.hasFile())throw new IllegalArgumentException("Файл счета не прикреплен");Resource resource=new FileSystemResource(resolve(invoice.getFileStorageName()));if(!resource.exists())throw new IllegalStateException("Файл счета отсутствует в хранилище");return resource;}
    private void normalize(SupplierInvoice invoice){invoice.setInvoiceNumber(trim(invoice.getInvoiceNumber()));if(invoice.getInvoiceNumber()==null)throw new IllegalArgumentException("Укажите номер счета");if(invoice.getInvoiceDate()==null)throw new IllegalArgumentException("Укажите дату счета");invoice.setNotes(trim(invoice.getNotes()));}
    private void positive(BigDecimal value){if(value==null||value.signum()<=0)throw new IllegalArgumentException("Количество должно быть положительным");}
    private void storeFile(SupplierInvoice invoice,MultipartFile file){if(file==null||file.isEmpty())return;String original=Optional.ofNullable(file.getOriginalFilename()).orElse("invoice");String clean=Path.of(original).getFileName().toString();String extension=extension(clean);if(!ALLOWED_EXTENSIONS.contains(extension))throw new IllegalArgumentException("Допустимы PDF, изображения, XLS и XLSX");String storageName=UUID.randomUUID()+"."+extension;try{Files.createDirectories(storageRoot);Files.copy(file.getInputStream(),resolve(storageName),StandardCopyOption.REPLACE_EXISTING);}catch(IOException exception){throw new IllegalStateException("Не удалось сохранить файл счета",exception);}invoice.setFileOriginalName(clean);invoice.setFileStorageName(storageName);invoice.setFileContentType(file.getContentType());}
    private void deleteStoredFile(SupplierInvoice invoice){if(invoice.hasFile())deleteStoredFile(invoice.getFileStorageName());}
    private void deleteStoredFile(String storageName){if(storageName==null||storageName.isBlank())return;try{Files.deleteIfExists(resolve(storageName));}catch(IOException exception){throw new IllegalStateException("Не удалось удалить файл счета",exception);}}
    private Path resolve(String storageName){Path resolved=storageRoot.resolve(storageName).normalize();if(!resolved.startsWith(storageRoot))throw new IllegalArgumentException("Некорректная ссылка на файл");return resolved;}
    private String extension(String name){int dot=name.lastIndexOf('.');return dot<0?"":name.substring(dot+1).toLowerCase(Locale.ROOT);}
    private String trim(String value){return value==null||value.isBlank()?null:value.trim();}
}
