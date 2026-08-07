package com.rustam.modern_dentistry.util;

import com.rustam.modern_dentistry.dao.entity.GeneralCalendar;
import com.rustam.modern_dentistry.dao.entity.enums.status.Appointment;
import com.rustam.modern_dentistry.dao.entity.enums.status.Status;
import com.rustam.modern_dentistry.dao.entity.patient_info.Invoice;
import com.rustam.modern_dentistry.dao.entity.patient_info.PatientReport;
import com.rustam.modern_dentistry.dao.entity.patient_info.Payment;
import com.rustam.modern_dentistry.dao.entity.settings.Cabinet;
import com.rustam.modern_dentistry.dao.entity.settings.permission.Permission;
import com.rustam.modern_dentistry.dao.entity.users.BaseUser;
import com.rustam.modern_dentistry.dao.entity.users.Patient;
import com.rustam.modern_dentistry.dao.repository.BaseUserRepository;
import com.rustam.modern_dentistry.dao.repository.GeneralCalendarRepository;
import com.rustam.modern_dentistry.dao.repository.PatientRepository;
import com.rustam.modern_dentistry.dao.repository.patient_info.InvoiceRepository;
import com.rustam.modern_dentistry.dao.repository.patient_info.PatientReportRepository;
import com.rustam.modern_dentistry.dao.repository.patient_info.PaymentRepository;
import com.rustam.modern_dentistry.dao.repository.settings.CabinetRepository;
import com.rustam.modern_dentistry.dao.repository.settings.PermissionRepository;
import com.rustam.modern_dentistry.dao.repository.settings.operations.OperationTypeRepository;
import com.rustam.modern_dentistry.dao.repository.settings.operations.OperationTypeItemRepository;
import com.rustam.modern_dentistry.dao.entity.settings.operations.OpType;
import com.rustam.modern_dentistry.dao.entity.settings.operations.OpTypeItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements CommandLineRunner {

    private final BaseUserRepository baseUserRepository;
    private final PatientRepository patientRepository;
    private final GeneralCalendarRepository generalCalendarRepository;
    private final PatientReportRepository patientReportRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final PermissionRepository permissionRepository;
    private final CabinetRepository cabinetRepository;
    private final PasswordEncoder passwordEncoder;
    private final OperationTypeRepository operationTypeRepository;
    private final OperationTypeItemRepository operationTypeItemRepository;

    @Override
    public void run(String... args) {
        log.info("Checking database seeder...");
        seedOperationTypesAndItems();
        if (patientRepository.count() > 0) {
            log.info("Database already seeded. Skipping seeder.");
            return;
        }

        log.info("Seeding database with mock clinical data for Reports Page...");


        // 1. Permissions
        Permission doctorPermission = permissionRepository.findByPermissionName("DOCTOR")
                .orElseGet(() -> permissionRepository.save(
                        Permission.builder().permissionName("DOCTOR").status(Status.ACTIVE).build()));

        Permission reportsPermission = permissionRepository.findByPermissionName("Hesabat:READ")
                .orElseGet(() -> permissionRepository.save(
                        Permission.builder().permissionName("Hesabat:READ").status(Status.ACTIVE).build()));

        Set<Permission> doctorPermissions = new HashSet<>(Arrays.asList(doctorPermission, reportsPermission));

        // 2. Doctors (BaseUser)
        BaseUser drMaryam = BaseUser.builder()
                .name("Məryəm")
                .surname("Səfərova")
                .email("maryam.s@clinic.com")
                .username("dr_maryam")
                .password(passwordEncoder.encode("123456"))
                .enabled(true)
                .colorCode("#10B981")
                .experience(8)
                .degree("PhD")
                .permissions(doctorPermissions)
                .build();
        baseUserRepository.save(drMaryam);

        BaseUser drElvin = BaseUser.builder()
                .name("Elvin")
                .surname("Əliyev")
                .email("elvin.a@clinic.com")
                .username("dr_elvin")
                .password(passwordEncoder.encode("123456"))
                .enabled(true)
                .colorCode("#3B82F6")
                .experience(5)
                .degree("Ortodont")
                .permissions(doctorPermissions)
                .build();
        baseUserRepository.save(drElvin);

        BaseUser drLaura = BaseUser.builder()
                .name("Laura")
                .surname("Sánchez Pérez")
                .email("laura.s@clinic.com")
                .username("dr_laura")
                .password(passwordEncoder.encode("123456"))
                .enabled(true)
                .colorCode("#8B5CF6")
                .experience(12)
                .degree("Cərrah")
                .permissions(doctorPermissions)
                .build();
        baseUserRepository.save(drLaura);

        // 3. Cabinets
        Cabinet cab1 = Cabinet.builder().cabinetName("Kabinet 1").status(Status.ACTIVE).build();
        Cabinet cab2 = Cabinet.builder().cabinetName("Kabinet 2").status(Status.ACTIVE).build();
        cabinetRepository.saveAll(Arrays.asList(cab1, cab2));

        // 4. Patients
        LocalDate today = LocalDate.now();
        Patient patElnur = Patient.builder()
                .name("Elnur")
                .surname("Quliyev")
                .phone("+994501111111")
                .email("elnur.q@gmail.com")
                .registrationDate(today.minusDays(5))
                .enabled(true)
                .baseUser(drMaryam)
                .build();
        patientRepository.save(patElnur);

        Patient patAysel = Patient.builder()
                .name("Aysel")
                .surname("Məmmədova")
                .phone("+994502222222")
                .email("aysel.m@gmail.com")
                .registrationDate(today.minusDays(12))
                .enabled(true)
                .baseUser(drElvin)
                .build();
        patientRepository.save(patAysel);

        Patient patMunoz = Patient.builder()
                .name("José Luis")
                .surname("Muñoz Blanco")
                .phone("+994503333333")
                .email("munoz@gmail.com")
                .registrationDate(today.minusMonths(2))
                .enabled(true)
                .baseUser(drLaura)
                .build();
        patientRepository.save(patMunoz);

        Patient patBorclu = Patient.builder()
                .name("Borclu")
                .surname("Pasiyent")
                .phone("+994504444444")
                .email("borclu@gmail.com")
                .registrationDate(today.minusDays(20))
                .enabled(true)
                .baseUser(drMaryam)
                .build();
        patientRepository.save(patBorclu);

        // 5. Appointments (GeneralCalendar)
        // Elnur - Completed
        GeneralCalendar app1 = GeneralCalendar.builder()
                .baseUser(drMaryam)
                .patient(patElnur)
                .cabinet(cab1)
                .date(today.minusDays(5))
                .time(LocalTime.of(10, 0))
                .appointment(Appointment.CAME)
                .build();
        // Aysel - Completed
        GeneralCalendar app2 = GeneralCalendar.builder()
                .baseUser(drElvin)
                .patient(patAysel)
                .cabinet(cab2)
                .date(today.minusDays(2))
                .time(LocalTime.of(14, 30))
                .appointment(Appointment.CAME)
                .build();
        // Munoz - Canceled (No-show / canceled)
        GeneralCalendar app3 = GeneralCalendar.builder()
                .baseUser(drLaura)
                .patient(patMunoz)
                .cabinet(cab1)
                .date(today.minusDays(8))
                .time(LocalTime.of(11, 0))
                .appointment(Appointment.CANCELED)
                .build();
        // Borclu - Canceled (No-show / canceled)
        GeneralCalendar app4 = GeneralCalendar.builder()
                .baseUser(drMaryam)
                .patient(patBorclu)
                .cabinet(cab1)
                .date(today.minusDays(1))
                .time(LocalTime.of(16, 0))
                .appointment(Appointment.CANCELED)
                .build();
        // Scheduled meeting for Elnur
        GeneralCalendar app5 = GeneralCalendar.builder()
                .baseUser(drMaryam)
                .patient(patElnur)
                .cabinet(cab2)
                .date(today.plusDays(3))
                .time(LocalTime.of(12, 0))
                .appointment(Appointment.MEETING)
                .build();
        generalCalendarRepository.saveAll(Arrays.asList(app1, app2, app3, app4, app5));

        // 6. Invoices
        // Invoice 1: Elnur (Maryam) - PAID
        Invoice inv1 = Invoice.builder()
                .invoiceNumber("FAC-2026-0001")
                .patient(patElnur)
                .doctor(drMaryam)
                .issueDate(today.minusDays(5))
                .dueDate(today.plusDays(10))
                .totalAmount(BigDecimal.valueOf(300.00))
                .vatRate(BigDecimal.ZERO)
                .vatAmount(BigDecimal.ZERO)
                .invoiceStatus("PAID")
                .status("A")
                .actionStatus("A")
                .build();
        invoiceRepository.save(inv1);

        // Invoice 2: Aysel (Elvin) - PAID
        Invoice inv2 = Invoice.builder()
                .invoiceNumber("FAC-2026-0002")
                .patient(patAysel)
                .doctor(drElvin)
                .issueDate(today.minusDays(12))
                .dueDate(today.minusDays(2))
                .totalAmount(BigDecimal.valueOf(180.00))
                .vatRate(BigDecimal.ZERO)
                .vatAmount(BigDecimal.ZERO)
                .invoiceStatus("PAID")
                .status("A")
                .actionStatus("A")
                .build();
        invoiceRepository.save(inv2);

        // Invoice 3: Munoz (Laura) - OVERDUE (due date was 15 days ago)
        Invoice inv3 = Invoice.builder()
                .invoiceNumber("FAC-2026-0007")
                .patient(patMunoz)
                .doctor(drLaura)
                .issueDate(today.minusDays(30))
                .dueDate(today.minusDays(15))
                .totalAmount(BigDecimal.valueOf(1195.00))
                .vatRate(BigDecimal.ZERO)
                .vatAmount(BigDecimal.ZERO)
                .invoiceStatus("PARTIAL")
                .status("A")
                .actionStatus("A")
                .build();
        invoiceRepository.save(inv3);

        // Invoice 4: Borclu (Maryam) - OVERDUE (due date was 5 days ago)
        Invoice inv4 = Invoice.builder()
                .invoiceNumber("FAC-2026-0008")
                .patient(patBorclu)
                .doctor(drMaryam)
                .issueDate(today.minusDays(10))
                .dueDate(today.minusDays(5))
                .totalAmount(BigDecimal.valueOf(687.50))
                .vatRate(BigDecimal.ZERO)
                .vatAmount(BigDecimal.ZERO)
                .invoiceStatus("PENDING")
                .status("A")
                .actionStatus("A")
                .build();
        invoiceRepository.save(inv4);

        // 7. Payments
        // Payment 1: inv1 (Elnur) - full (300.00) using Kart
        Payment pay1 = Payment.builder()
                .invoice(inv1)
                .amount(BigDecimal.valueOf(300.00))
                .paymentMethod("Kart")
                .paymentDate(today.minusDays(5))
                .status("A")
                .actionStatus("A")
                .build();
        // Payment 2: inv2 (Aysel) - full (180.00) using Nəğd
        Payment pay2 = Payment.builder()
                .invoice(inv2)
                .amount(BigDecimal.valueOf(180.00))
                .paymentMethod("Nəğd")
                .paymentDate(today.minusDays(12))
                .status("A")
                .actionStatus("A")
                .build();
        // Payment 3: inv3 (Munoz) - partial (780.00) using Direct debit
        Payment pay3 = Payment.builder()
                .invoice(inv3)
                .amount(BigDecimal.valueOf(780.00))
                .paymentMethod("Direct debit")
                .paymentDate(today.minusDays(29))
                .status("A")
                .actionStatus("A")
                .build();
        paymentRepository.saveAll(Arrays.asList(pay1, pay2, pay3));

        // 8. PatientReport logs (Treatment logs)
        // Elnur - Maryland/Implant planned & completed
        PatientReport rep1 = PatientReport.builder()
                .patientId(patElnur.getId())
                .planDate(today.minusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .patientName(patElnur.getName() + " " + patElnur.getSurname())
                .teethNo(12L)
                .operationName("Implant")
                .planningDoctorName(drMaryam.getName() + " " + drMaryam.getSurname())
                .price(BigDecimal.valueOf(350.00))
                .discount(BigDecimal.valueOf(50.00))
                .finalPrice(BigDecimal.valueOf(300.00))
                .executionDate(today.minusDays(5).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .executionDoctorName(drMaryam.getName() + " " + drMaryam.getSurname())
                .build();

        // Aysel - Extraction completed
        PatientReport rep2 = PatientReport.builder()
                .patientId(patAysel.getId())
                .planDate(today.minusDays(15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .patientName(patAysel.getName() + " " + patAysel.getSurname())
                .teethNo(24L)
                .operationName("Diş Çəkimi")
                .planningDoctorName(drElvin.getName() + " " + drElvin.getSurname())
                .price(BigDecimal.valueOf(180.00))
                .discount(BigDecimal.ZERO)
                .finalPrice(BigDecimal.valueOf(180.00))
                .executionDate(today.minusDays(12).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .executionDoctorName(drElvin.getName() + " " + drElvin.getSurname())
                .build();

        // Munoz - Bridge planned and completed
        PatientReport rep3 = PatientReport.builder()
                .patientId(patMunoz.getId())
                .planDate(today.minusDays(40).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .patientName(patMunoz.getName() + " " + patMunoz.getSurname())
                .teethNo(16L)
                .operationName("Körpü Diş")
                .planningDoctorName(drLaura.getName() + " " + drLaura.getSurname())
                .price(BigDecimal.valueOf(1300.00))
                .discount(BigDecimal.valueOf(105.00))
                .finalPrice(BigDecimal.valueOf(1195.00))
                .executionDate(today.minusDays(30).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .executionDoctorName(drLaura.getName() + " " + drLaura.getSurname())
                .build();

        // Borclu Pasiyent - canal planned & completed
        PatientReport rep4 = PatientReport.builder()
                .patientId(patBorclu.getId())
                .planDate(today.minusDays(15).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .patientName(patBorclu.getName() + " " + patBorclu.getSurname())
                .teethNo(11L)
                .operationName("Kanal Müalicəsi")
                .planningDoctorName(drMaryam.getName() + " " + drMaryam.getSurname())
                .price(BigDecimal.valueOf(750.00))
                .discount(BigDecimal.valueOf(62.50))
                .finalPrice(BigDecimal.valueOf(687.50))
                .executionDate(today.minusDays(10).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .executionDoctorName(drMaryam.getName() + " " + drMaryam.getSurname())
                .build();

        patientReportRepository.saveAll(Arrays.asList(rep1, rep2, rep3, rep4));
        log.info("Database successfully seeded!");
    }

    private void seedOperationTypesAndItems() {
        if (operationTypeRepository.count() == 0) {
            log.info("Seeding Operation Types and Items...");

            OpType toothCategory = OpType.builder()
                    .categoryName("Diş")
                    .categoryCode("DIS")
                    .colorSelection(true)
                    .implantSelection(true)
                    .status(Status.ACTIVE)
                    .build();

            toothCategory = operationTypeRepository.save(toothCategory);

            String[] operations = {
                "Diş çəkimi",
                "Diş təmizlənməsi",
                "Diş müalicəsi",
                "Diş protezləşdirilməsi",
                "Diş implantı",
                "Diş bərpası",
                "Digər"
            };

            List<OpTypeItem> items = new ArrayList<>();
            for (int i = 0; i < operations.length; i++) {
                items.add(OpTypeItem.builder()
                        .operationName(operations[i])
                        .operationCode("DIS_" + (i + 1))
                        .status(Status.ACTIVE)
                        .amount(BigDecimal.valueOf(100.00 + (i * 50)))
                        .opType(toothCategory)
                        .build());
            }
            operationTypeItemRepository.saveAll(items);
            log.info("Operation Types and Items seeded successfully!");
        }
    }
}
