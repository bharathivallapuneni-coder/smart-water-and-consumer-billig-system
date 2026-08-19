package com.infosys.smartwater.service;

import com.infosys.smartwater.dto.request.ActivateResidentRequest;
import com.infosys.smartwater.dto.request.InviteResidentRequest;
import com.infosys.smartwater.dto.response.InvitationValidateResponse;
import com.infosys.smartwater.entity.Apartment;
import com.infosys.smartwater.entity.Household;
import com.infosys.smartwater.entity.InvitationToken;
import com.infosys.smartwater.entity.User;
import com.infosys.smartwater.entity.enums.ApprovalStatus;
import com.infosys.smartwater.entity.enums.Role;
import com.infosys.smartwater.exception.InvalidOperationException;
import com.infosys.smartwater.exception.ResourceNotFoundException;
import com.infosys.smartwater.repository.ApartmentRepository;
import com.infosys.smartwater.repository.HouseholdRepository;
import com.infosys.smartwater.repository.InvitationTokenRepository;
import com.infosys.smartwater.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResidentInvitationService {

    private final HouseholdRepository householdRepository;
    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;
    private final InvitationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Transactional
    public String inviteResident(UUID buildingOwnerUserId, InviteResidentRequest request) {
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new InvalidOperationException("Full Name is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new InvalidOperationException("Email ID is required");
        }
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            throw new InvalidOperationException("Phone Number is required");
        }
        if (request.getFlatNumber() == null || request.getFlatNumber().isBlank()) {
            throw new InvalidOperationException("Flat Number is required");
        }

        // Resolve Apartment
        Apartment apartment = null;
        if (request.getBuildingId() != null) {
            apartment = apartmentRepository.findById(request.getBuildingId()).orElse(null);
        }
        if (apartment == null && buildingOwnerUserId != null) {
            apartment = apartmentRepository.findByBuildingOwnerId(buildingOwnerUserId).orElse(null);
        }
        if (apartment == null) {
            apartment = apartmentRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Apartment", "buildingOwnerId", buildingOwnerUserId));
        }

        String householdNum = (request.getBlockNumber() != null && !request.getBlockNumber().isBlank())
                ? request.getBlockNumber() + "-" + request.getFlatNumber()
                : request.getFlatNumber();

        Household household = householdRepository.findByHouseholdNumber(householdNum)
                .orElseGet(() -> householdRepository.findByHouseholdNumber(request.getFlatNumber()).orElse(null));

        if (household == null) {
            household = Household.builder()
                    .householdNumber(householdNum)
                    .ownerName(request.getFullName())
                    .contactPhone(request.getPhone())
                    .blockNumber(request.getBlockNumber())
                    .apartment(apartment)
                    .isActive(true)
                    .invitationStatus("PENDING")
                    .build();
        } else {
            household.setOwnerName(request.getFullName());
            household.setContactPhone(request.getPhone());
            if (request.getBlockNumber() != null) {
                household.setBlockNumber(request.getBlockNumber());
            }
            household.setInvitationStatus("PENDING");
        }

        householdRepository.save(household);

        // Delete any prior unused token for this household
        tokenRepository.deleteByHouseholdId(household.getId());

        // Generate Secure Unique Token
        String tokenString = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");

        InvitationToken tokenEntity = InvitationToken.builder()
                .household(household)
                .apartment(apartment)
                .token(tokenString)
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .phone(request.getPhone())
                .flatNumber(request.getFlatNumber())
                .blockNumber(request.getBlockNumber())
                .expiryDate(LocalDateTime.now().plusHours(48))
                .isUsed(false)
                .build();

        tokenRepository.save(tokenEntity);

        String activationUrl = frontendBaseUrl + "/resident/activate?token=" + tokenString;
        String subject = "You're invited to join HydroBill";
        String htmlBody = String.format(
                "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 12px; background-color: #ffffff;\">" +
                        "<h2 style=\"color: #0284c7; margin-top: 0;\">You're invited to join HydroBill</h2>" +
                        "<p style=\"font-size: 15px; color: #334155;\">Hello <strong>%s</strong>,</p>" +
                        "<p style=\"font-size: 15px; color: #334155;\">You have been invited by your Building Owner to join HydroBill.</p>" +
                        "<p style=\"font-size: 15px; color: #334155;\">Click the button below to create your HydroBill account:</p>" +
                        "<div style=\"margin: 28px 0; text-align: center;\">" +
                        "<a href=\"%s\" style=\"background-color: #0284c7; color: #ffffff; padding: 12px 28px; font-weight: bold; text-decoration: none; border-radius: 8px; display: inline-block; font-size: 16px;\">Create Your Account</a>" +
                        "</div>" +
                        "<p style=\"font-size: 13px; color: #64748b;\">Or copy and paste this link into your browser:<br/><a href=\"%s\" style=\"color: #0284c7;\">%s</a></p>" +
                        "<hr style=\"border: none; border-top: 1px solid #e2e8f0; margin: 24px 0;\" />" +
                        "<p style=\"font-size: 12px; color: #94a3b8;\">This invitation link is unique to you and will expire after 48 hours.<br/>If you did not expect this invitation, please contact your Building Owner.</p>" +
                        "</div>",
                request.getFullName(),
                activationUrl,
                activationUrl,
                activationUrl
        );

        log.info("Creating resident invitation for email: {}", request.getEmail());
        log.info("Invitation token generated successfully for flat {}", householdNum);

        emailService.sendEmailAlert(request.getEmail().toLowerCase().trim(), subject, htmlBody);

        return "Invitation email successfully sent to " + request.getEmail();
    }

    @Transactional(readOnly = true)
    public InvitationValidateResponse validateToken(String token) {
        if (token == null || token.isBlank()) {
            return InvitationValidateResponse.builder()
                    .isValid(false)
                    .message("Invitation token is required")
                    .build();
        }

        InvitationToken tokenEntity = tokenRepository.findByToken(token.trim()).orElse(null);

        if (tokenEntity == null) {
            return InvitationValidateResponse.builder()
                    .isValid(false)
                    .message("Invalid invitation link.")
                    .build();
        }

        if (Boolean.TRUE.equals(tokenEntity.getIsUsed())) {
            return InvitationValidateResponse.builder()
                    .isValid(false)
                    .message("This invitation link has already been used.")
                    .build();
        }

        if (tokenEntity.isExpired()) {
            return InvitationValidateResponse.builder()
                    .isValid(false)
                    .message("This invitation link has expired.")
                    .build();
        }

        String buildingName = tokenEntity.getApartment() != null ? tokenEntity.getApartment().getBuildingName() : "HydroBill Property";

        return InvitationValidateResponse.builder()
                .token(tokenEntity.getToken())
                .fullName(tokenEntity.getFullName())
                .email(tokenEntity.getEmail())
                .flatNumber(tokenEntity.getFlatNumber())
                .blockNumber(tokenEntity.getBlockNumber())
                .buildingName(buildingName)
                .isValid(true)
                .message("Token is valid")
                .build();
    }

    @Transactional
    public void activateAccount(ActivateResidentRequest request) {
        if (request.getToken() == null || request.getToken().isBlank()) {
            throw new InvalidOperationException("Invitation token is required");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new InvalidOperationException("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new InvalidOperationException("Password is required");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidOperationException("Password and Confirm Password do not match");
        }

        InvitationToken tokenEntity = tokenRepository.findByToken(request.getToken().trim())
                .orElseThrow(() -> new InvalidOperationException("Invalid or expired invitation link"));

        if (Boolean.TRUE.equals(tokenEntity.getIsUsed())) {
            throw new InvalidOperationException("This invitation link has already been used");
        }

        if (tokenEntity.isExpired()) {
            throw new InvalidOperationException("This invitation link has expired");
        }

        String usernameClean = request.getUsername().trim();
        if (userRepository.existsByUsername(usernameClean)) {
            throw new InvalidOperationException("Username '" + usernameClean + "' is already taken. Please choose another username.");
        }

        Household household = tokenEntity.getHousehold();
        if (household == null) {
            throw new InvalidOperationException("Household record not found for invitation token");
        }

        // Check if user with this email exists
        User user = userRepository.findByEmail(tokenEntity.getEmail().toLowerCase().trim()).orElse(null);

        if (user == null) {
            user = User.builder()
                    .username(usernameClean)
                    .email(tokenEntity.getEmail().toLowerCase().trim())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .phone(tokenEntity.getPhone())
                    .role(Role.RESIDENT)
                    .isEnabled(true)
                    .approvalStatus(ApprovalStatus.APPROVED)
                    .build();
        } else {
            user.setUsername(usernameClean);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setIsEnabled(true);
            user.setApprovalStatus(ApprovalStatus.APPROVED);
        }

        userRepository.save(user);

        household.setUser(user);
        household.setOwnerName(tokenEntity.getFullName());
        household.setInvitationStatus("ACCEPTED");
        householdRepository.save(household);

        tokenEntity.setIsUsed(true);
        tokenRepository.save(tokenEntity);

        log.info("Successfully activated resident account username '{}' for household '{}'", usernameClean, household.getHouseholdNumber());
    }
}
