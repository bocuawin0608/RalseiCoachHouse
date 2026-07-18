import { useState, useEffect } from 'react';
import { Row, Col, Form, Card, Alert, Spinner, OverlayTrigger, Tooltip, InputGroup, Button, Modal } from 'react-bootstrap';
import { BsExclamationTriangleFill, BsPersonFill, BsMapFill, BsTicketPerforatedFill, BsInfoCircle, BsCashStack } from "react-icons/bs";
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { useForm, useFieldArray } from 'react-hook-form';
import { useAuth } from '../../auth';
import { setPassengerInfo, setPaymentInfo } from '../reducers/bookingSlice';
import { useStep2InitData } from '../hooks/useStep2InitData';
import { usePriceCalculation } from '../hooks/usePriceCalculation';
import { usePhoneVerification } from '../hooks/usePhoneVerification';
import { formatCurrency, formatDateTime } from '../../../utils/formatters';
import { bookingApi } from '../api/bookingApi';
import { transformFormToPassengerPayload } from '../utils/passengerPayload';
import { mapConfirmResponse, savePaymentSession } from '../utils/paymentSession';
import { bookingValidationRules, validateChildBirthYearValue } from '../utils/bookingValidation';
import { buildTripShellLabels, computePickupPresentBy, formatPickupPresentByLabel } from '../utils/tripInfo';
import TripSummaryPanel from './TripSummaryPanel';
import PhoneOtpModal from './PhoneOtpModal';
import BookingTermsConsent from './BookingTermsConsent';

export default function Step2PassengerInfo({ tripId }) {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const { selectedSeats, holdToken, tripInfo } = useSelector(state => state.booking);
    const { token, user } = useAuth();
    const isAuthenticated = Boolean(token && user);

    const { initData, loading, error } = useStep2InitData(tripId, holdToken);
    const {
        verifiedPhones,
        phoneCheckLoading,
        phoneCheckError,
        otpPhone,
        markKnownPhone,
        clearPhoneVerification,
        isPhoneVerified,
        getUnverifiedPhones,
        handleOtpVerified,
        closeOtpModal,
        checkPhoneOnBlur,
        openOtpForPhone,
    } = usePhoneVerification();

    const [showVoucherModal, setShowVoucherModal] = useState(false);
    const [typedVoucherCode, setTypedVoucherCode] = useState('');
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState('');
    
    const { register, handleSubmit, control, watch, setValue, getValues, formState: { errors } } = useForm({
        defaultValues: {
            pickupStopId: '',
            dropoffStopId: '',
            voucherId: '',
            acceptTerms: false,
            passengers: selectedSeats.map(seatObj => ({
                tripSeatId: seatObj.tripSeatId,
                seatCode: seatObj.seatCode,
                fullname: '',
                phone: '',
                email: '',
                hasChild: false, 
                childName: '',
                childBirthYear: ''
            }))
        }
    });

    const { fields } = useFieldArray({ control, name: 'passengers' });

    const currentVoucherId = watch('voucherId');
    const effectiveVoucherId = isAuthenticated ? currentVoucherId : '';
    const pickupStopId = watch('pickupStopId');
    const dropoffStopId = watch('dropoffStopId');

    const { priceData, loading: priceLoading, error: priceError } = usePriceCalculation(tripId, holdToken, {
        pickupStopId,
        dropoffStopId,
        voucherId: effectiveVoucherId,
    });

    const seatCount = selectedSeats.length;
    const seatCodes = selectedSeats.map((seat) => seat.seatCode).filter(Boolean);
    const pickupStop = initData.pickupStopPoints?.find((point) => String(point.stopPointId) === String(pickupStopId));
    const dropoffStop = initData.dropoffStopPoints?.find((point) => String(point.stopPointId) === String(dropoffStopId));
    const pickupPresentBy = pickupStop && tripInfo?.departureTime
        ? computePickupPresentBy(tripInfo.departureTime, pickupStop.minutesFromStart)
        : null;

    useEffect(() => {
        if (!isAuthenticated) {
            setValue('voucherId', '');
            setTypedVoucherCode('');
            setShowVoucherModal(false);
            return;
        }
        if (!currentVoucherId) {
            setTypedVoucherCode('');
            return;
        }
        const selected = initData.vouchers?.find(v => v.voucherId == currentVoucherId);
        if (selected) {
            setTypedVoucherCode(selected.voucherCode);
        }
    }, [currentVoucherId, initData.vouchers, isAuthenticated, setValue]);

    useEffect(() => {
        if (!initData?.customerProfile || fields.length === 0) {
            return;
        }

        const profile = initData.customerProfile;
        setValue('passengers.0.fullname', profile.fullname?.trim() || '');
        setValue('passengers.0.phone', profile.phone?.trim() || '');
        setValue('passengers.0.email', profile.email?.trim() || '');
        if (profile.phone?.trim()) {
            markKnownPhone(profile.phone.trim());
        }
    }, [initData?.customerProfile, fields.length, setValue, markKnownPhone]);

    const handleSelectVoucher = (voucher) => {
        if (!isAuthenticated) return;
        setValue('voucherId', voucher.voucherId);
    };

    const handleClearVoucher = () => {
        setValue('voucherId', '');
        setTypedVoucherCode('');
    };
    
    const formatDiscountBadge = (voucher) => {
        return voucher.discountType === 'PERCENT' ? `${voucher.discountValue}%` : `${voucher.discountValue / 1000}K`;
    };

    const handleApplyManualVoucher = () => {
        if (!isAuthenticated) return;
        if (!typedVoucherCode.trim()) return;
        const matched = initData.vouchers?.find(v => v.voucherCode.toLowerCase() === typedVoucherCode.trim().toLowerCase());
        if (matched) {
            setValue('voucherId', matched.voucherId);
            alert(`Áp dụng mã ${matched.voucherCode} thành công!`);
        } else {
            alert("Mã giảm giá không tồn tại hoặc đã hết hạn.");
        }
    };

    const onSubmit = async (formData) => {
        setSubmitError('');

        const unverifiedPhones = getUnverifiedPhones(formData.passengers);
        if (unverifiedPhones.length > 0) {
            setSubmitError(`Vui lòng xác thực OTP cho số điện thoại: ${unverifiedPhones.join(', ')}`);
            return;
        }

        const formattedPassengers = transformFormToPassengerPayload(formData.passengers, verifiedPhones);

        const payload = {
            pickupStopId: Number(formData.pickupStopId),
            dropoffStopId: Number(formData.dropoffStopId),
            voucherId: isAuthenticated && formData.voucherId ? Number(formData.voucherId) : null,
            passengers: formattedPassengers,
        };

        setSubmitting(true);
        try {
            const response = await bookingApi.confirmBooking(
                tripId,
                payload,
                holdToken
            );

            const { tripTitle, tripDate } = buildTripShellLabels(tripInfo);
            const paymentInfo = mapConfirmResponse(response, {
                tripId: Number(tripId),
                primaryPassengerName: formattedPassengers[0]?.fullname,
                primaryPassengerPhone: formattedPassengers[0]?.phone,
                seatCodes,
                tripTitle,
                tripDate
            });

            dispatch(setPassengerInfo(payload));
            dispatch(setPaymentInfo(paymentInfo));
            savePaymentSession(response.transactionId, paymentInfo);
            navigate(`/booking/payment/${response.transactionId}`);
        } catch (err) {
            setSubmitError(err.response?.data?.message || 'Không thể xác nhận đặt vé. Vui lòng thử lại!');
            window.scrollTo(0, 0);
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) return <div className="text-center py-5"><Spinner animation="border" /></div>;

    return (
        <div>
            {error && (
                <Alert variant='danger' className="mb-4 d-flex align-items-center gap-2 rounded-3 border-0 shadow-sm" style={{ fontSize: '0.9rem' }}>
                    <BsExclamationTriangleFill /> {error}
                </Alert>
            )}

            {phoneCheckError && (
                <Alert variant="warning" className="mb-4 rounded-3 border-0 shadow-sm" style={{ fontSize: '0.9rem' }}>
                    {phoneCheckError}
                </Alert>
            )}

            {submitError && (
                <Alert variant='danger' className="mb-4 d-flex align-items-center gap-2 rounded-3 border-0 shadow-sm" style={{ fontSize: '0.9rem' }}>
                    <BsExclamationTriangleFill /> {submitError}
                </Alert>
            )}

            {/* Hook form lo việc handleSubmit và ngăn e.preventDefault tự động */}
            <Form onSubmit={handleSubmit(onSubmit)}>
                <Row className="g-4">
                    {/* CỘT TRÁI */}
                    <Col lg={8} md={12}>
                        {/* THÔNG TIN HÀNH KHÁCH */}
                        <div className=" ms-2 fw-bold d-flex align-items-center gap-2 mb-3" style={{ fontSize: '1.05rem', color: 'var(--ralsei-black)' }}>
                            <BsPersonFill size={18} /> Thông tin hành khách
                        </div>
                        
                        {fields.map((field, index) => {
                            const phoneRegister = register(`passengers.${index}.phone`, bookingValidationRules.phone);
                            const passengerPhone = watch(`passengers.${index}.phone`);
                            const phoneVerified = isPhoneVerified(passengerPhone);
                            const phoneVerification = passengerPhone?.trim() ? verifiedPhones[passengerPhone.trim()] : null;

                            return (
                            <Card key={field.id} className="border-0 rounded-3 mb-3 bg-white overflow-hidden" style={{ border: '1px solid #f0f0f0', boxShadow: '0 4px 12px rgba(0, 0, 0, 0.12), 0 1px 3px rgba(0, 0, 0, 0.08)' }}>
                                <Card.Header className="bg-white p-3 fw-semibold text-secondary d-flex justify-content-between align-items-center" style={{ fontSize: '0.9rem' }}>
                                    <span>Hành khách ngồi <span className="fw-bold" style={{ color: 'var(--ralsei-black)' }}>vị trí {field.seatCode}</span></span>
                                    <span className="badge rounded-pill fw-normal text-white" style={{ backgroundColor: 'var(--ralsei-black)' }}>
                                        Ghế {field.seatCode}
                                    </span>
                                </Card.Header>
                                <Card.Body className="p-3">
                                    <Row className="g-3">
                                        <Form.Group as={Col} md={6}>
                                            <Form.Label className="fw-medium text-muted mb-1" style={{ fontSize: '0.85rem' }}>Họ và tên <span className="text-danger">*</span></Form.Label>
                                            <Form.Control 
                                                {...register(`passengers.${index}.fullname`, bookingValidationRules.fullname)}
                                                isInvalid={!!errors.passengers?.[index]?.fullname}
                                                type="text" placeholder="VD: Nguyễn Văn A" className="rounded-3 shadow-none" style={{ fontSize: '0.9rem' }}
                                            />
                                            <Form.Control.Feedback type="invalid">{errors.passengers?.[index]?.fullname?.message}</Form.Control.Feedback>
                                        </Form.Group>

                                        <Form.Group as={Col} md={6}>
                                            <Form.Label className="fw-medium text-muted mb-1" style={{ fontSize: '0.85rem' }}>Số điện thoại <span className="text-danger">*</span></Form.Label>
                                            <Form.Control 
                                                {...phoneRegister}
                                                onBlur={(event) => {
                                                    phoneRegister.onBlur(event);
                                                    checkPhoneOnBlur(event.target.value, index, setValue, getValues);
                                                }}
                                                onChange={(event) => {
                                                    const prevTrimmed = watch(`passengers.${index}.phone`)?.trim();
                                                    phoneRegister.onChange(event);
                                                    if (prevTrimmed) {
                                                        const othersUsing = getValues('passengers')
                                                            .filter((p, i) => i !== index && p.phone?.trim() === prevTrimmed);
                                                        if (othersUsing.length === 0) {
                                                            clearPhoneVerification(prevTrimmed);
                                                        }
                                                    }
                                                }}
                                                isInvalid={!!errors.passengers?.[index]?.phone}
                                                type="tel" placeholder="VD: 0912345678" className="rounded-3 shadow-none" style={{ fontSize: '0.9rem' }}
                                            />
                                            <Form.Control.Feedback type="invalid">{errors.passengers?.[index]?.phone?.message}</Form.Control.Feedback>
                                            <div className="d-flex align-items-center justify-content-between mt-1 gap-2">
                                                {phoneCheckLoading === passengerPhone?.trim() ? (
                                                    <small className="text-muted">Đang kiểm tra số điện thoại...</small>
                                                ) : phoneVerified ? (
                                                    <small className="text-success">
                                                        {phoneVerification?.isKnown ? 'Số điện thoại đã biết' : 'Đã xác thực OTP'}
                                                    </small>
                                                ) : (
                                                    <small className="text-warning">Chưa xác thực số điện thoại</small>
                                                )}
                                                {!phoneVerified && passengerPhone?.trim() && !phoneCheckLoading && (
                                                    <Button
                                                        type="button"
                                                        size="sm"
                                                        variant="outline-dark"
                                                        className="rounded-pill px-3"
                                                        style={{ fontSize: '0.75rem' }}
                                                        onClick={() => openOtpForPhone(passengerPhone.trim())}
                                                    >
                                                        Xác thực OTP
                                                    </Button>
                                                )}
                                            </div>
                                        </Form.Group>

                                        <Form.Group as={Col} md={12}>
                                            <Form.Label className="fw-medium text-muted mb-1" style={{ fontSize: '0.85rem' }}>Email (Nhận vé điện tử) <span className="text-danger">*</span></Form.Label>
                                            <Form.Control 
                                                {...register(`passengers.${index}.email`, bookingValidationRules.email)}
                                                isInvalid={!!errors.passengers?.[index]?.email}
                                                type="email" placeholder="VD: name@example.com" className="rounded-3 shadow-none" style={{ fontSize: '0.9rem' }}
                                            />
                                            <Form.Control.Feedback type="invalid">{errors.passengers?.[index]?.email?.message}</Form.Control.Feedback>
                                        </Form.Group>
                                    </Row>

                                    {/* PHẦN TRẺ EM ĐI KÈM */}
                                    <div className="mt-3 pt-3 border-top">
                                        <Form.Check 
                                            {...register(`passengers.${index}.hasChild`)}
                                            type="switch"
                                            id={`child-check-${field.id}`}
                                            label="Có trẻ nhỏ đi cùng (Chiều cao từ 1m1 trở xuống)"
                                            className="fw-medium text-muted" style={{ fontSize: '0.85rem' }}
                                        />

                                        {/* RHF Watch: Lắng nghe trạng thái checkbox của hành khách hiện tại để render form trẻ em */}
                                        {watch(`passengers.${index}.hasChild`) && (
                                            <Row className="g-2 mt-2 bg-light p-3 rounded-3 border border-dashed" style={{ borderColor: '#e0e0e0' }}>
                                                <Form.Group as={Col} md={7}>
                                                    <Form.Label className="fw-medium text-muted mb-1" style={{ fontSize: '0.85rem' }}>Họ tên bé <span className="text-danger">*</span></Form.Label>
                                                    <Form.Control 
                                                        {...register(`passengers.${index}.childName`, {
                                                            ...bookingValidationRules.childName,
                                                            validate: (value, formValues) => {
                                                                if (!formValues.passengers?.[index]?.hasChild) return true;
                                                                if (!value) return bookingValidationRules.childName.required;
                                                                return bookingValidationRules.childName.pattern.value.test(value)
                                                                    ? true
                                                                    : bookingValidationRules.childName.pattern.message;
                                                            },
                                                        })}
                                                        isInvalid={!!errors.passengers?.[index]?.childName}
                                                        type="text" placeholder="Tên của bé" className="bg-white rounded-3 shadow-none" style={{ fontSize: '0.9rem' }}
                                                    />
                                                    <Form.Control.Feedback type="invalid">{errors.passengers?.[index]?.childName?.message}</Form.Control.Feedback>
                                                </Form.Group>
                                                <Form.Group as={Col} md={5}>
                                                    <Form.Label className="fw-medium text-muted mb-1" style={{ fontSize: '0.85rem' }}>Năm sinh <span className="text-danger">*</span></Form.Label>
                                                    <Form.Control 
                                                        {...register(`passengers.${index}.childBirthYear`, {
                                                            validate: (value, formValues) => {
                                                                if (!formValues.passengers?.[index]?.hasChild) return true;
                                                                if (!value && value !== 0) return bookingValidationRules.childBirthYear.required;
                                                                return validateChildBirthYearValue(value);
                                                            },
                                                        })}
                                                        isInvalid={!!errors.passengers?.[index]?.childBirthYear}
                                                        type="number" className="bg-white rounded-3 shadow-none" style={{ fontSize: '0.9rem' }}
                                                    />
                                                    <Form.Control.Feedback type="invalid">{errors.passengers?.[index]?.childBirthYear?.message}</Form.Control.Feedback>
                                                </Form.Group>
                                            </Row>
                                        )}
                                    </div>
                                </Card.Body>
                            </Card>
                            );
                        })}

                        {/* ĐIỂM ĐÓN VÀ TRẢ */}
                        <div className="mt-4 pt-2">
                            <div className="ms-2 fw-bold d-flex align-items-center gap-2 mb-3" style={{ fontSize: '1.05rem', color: 'var(--ralsei-black)' }}>
                                <BsMapFill size={16} /> Chặng đường di chuyển
                            </div>
                            <Card className="border-0 rounded-3 bg-white p-3" style={{ border: '1px solid #f0f0f0', boxShadow: '0 4px 12px rgba(0, 0, 0, 0.12), 0 1px 3px rgba(0, 0, 0, 0.08)' }}>
                                <Row className="g-3">
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="fw-medium text-muted mb-1" style={{ fontSize: '0.85rem' }}>Điểm đón khách <span className="text-danger">*</span></Form.Label>
                                            <Form.Select 
                                                {...register('pickupStopId', { required: 'Vui lòng chọn điểm đón' })}
                                                isInvalid={!!errors.pickupStopId}
                                                className="rounded-3 py-2 shadow-none" style={{ fontSize: '0.9rem' }}
                                            >
                                                <option value="">Chọn điểm đón</option>
                                                {initData.pickupStopPoints?.map(point => (
                                                    <option key={point.stopPointId} value={point.stopPointId}>{point.stopPointName}</option>
                                                ))}
                                            </Form.Select>
                                            <Form.Control.Feedback type="invalid">{errors.pickupStopId?.message}</Form.Control.Feedback>
                                            {pickupStop && pickupPresentBy && (
                                                <Alert variant="info" className="mt-2 mb-0 py-2 px-3 rounded-3 border-0" style={{ fontSize: '0.8rem' }}>
                                                    Quý khách vui lòng có mặt tại <strong>{pickupStop.stopPointName}</strong> trước <strong>{formatPickupPresentByLabel(pickupPresentBy)}</strong> để được trung chuyển hoặc kiểm tra thông tin trước khi lên xe.
                                                </Alert>
                                            )}
                                        </Form.Group>
                                    </Col>
                                    <Col md={6}>
                                        <Form.Group>
                                            <Form.Label className="fw-medium text-muted mb-1" style={{ fontSize: '0.85rem' }}>Điểm trả khách <span className="text-danger">*</span></Form.Label>
                                            <Form.Select 
                                                {...register('dropoffStopId', { required: 'Vui lòng chọn điểm trả' })}
                                                isInvalid={!!errors.dropoffStopId}
                                                className="rounded-3 py-2 shadow-none" style={{ fontSize: '0.9rem' }}
                                            >
                                                <option value="">Chọn điểm trả</option>
                                                {initData.dropoffStopPoints?.map(point => (
                                                    <option key={point.stopPointId} value={point.stopPointId}>{point.stopPointName}</option>
                                                ))}
                                            </Form.Select>
                                            <Form.Control.Feedback type="invalid">{errors.dropoffStopId?.message}</Form.Control.Feedback>
                                        </Form.Group>
                                    </Col>
                                </Row>
                            </Card>
                        </div>

                        <div className="mt-4 pt-2">
                            <BookingTermsConsent
                                register={register}
                                error={errors.acceptTerms}
                            />
                        </div>
                    </Col>

                    {/* CỘT PHẢI: THÔNG TIN CHUYẾN + VOUCHER */}
                    <Col lg={4} md={12}>
                        <Card className="border-0 rounded-4 shadow-sm bg-white p-4 sticky-top" style={{ top: '20px', zIndex: 10, border: '1px solid #f0f0f0' }}>
                            <TripSummaryPanel
                                tripInfo={tripInfo}
                                pickupStopName={pickupStop?.stopPointName}
                                dropoffStopName={dropoffStop?.stopPointName}
                                seatCount={seatCount}
                                seatCodes={seatCodes}
                            />
                            
                            {isAuthenticated ? (
                                <>
                                    <div className="d-flex justify-content-between align-items-center mb-3">
                                        <div className="fw-bold m-0 d-flex align-items-center gap-2" style={{ fontSize: '1.05rem', color: 'var(--ralsei-black)' }}>
                                            <BsTicketPerforatedFill size={18} /> Mã giảm giá
                                        </div>
                                        <span className="fw-semibold" style={{ fontSize: '0.85rem', color: 'var(--ralsei-black)', cursor: 'pointer' }} onClick={() => setShowVoucherModal(true)}>
                                            Xem tất cả
                                        </span>
                                    </div>

                                    <InputGroup className="mb-3">
                                        <Form.Control
                                            placeholder="Nhập mã giảm giá..."
                                            className="rounded-start-3 shadow-none border-end-0" style={{ fontSize: '0.9rem' }}
                                            value={typedVoucherCode} onChange={e => setTypedVoucherCode(e.target.value)}
                                        />
                                        <Button className="rounded-end-3 fw-semibold border-start-0 booking-btn-general2" style={{ fontSize: '0.9rem' }} onClick={handleApplyManualVoucher}>
                                            Áp dụng
                                        </Button>
                                    </InputGroup>

                                    <div className="voucher-list d-flex flex-column gap-2 mb-4">
                                        {initData.vouchers?.slice(0, 2).map(voucher => {
                                            const isSelected = currentVoucherId == voucher.voucherId;
                                            return (
                                                <div 
                                                    key={voucher.voucherId} 
                                                    className={`d-flex border rounded-3 overflow-hidden align-items-center ${isSelected ? 'bg-light' : ''}`}
                                                    style={{ cursor: 'pointer', transition: 'all 0.2s', borderColor: isSelected ? 'var(--ralsei-black)' : '#e0e0e0', borderWidth: isSelected ? '2px' : '1px' }}
                                                    onClick={() => handleSelectVoucher(voucher)} 
                                                >
                                                    <div className="text-white fw-bold d-flex align-items-center justify-content-center p-2 h-100" style={{ minWidth: '65px', minHeight: '70px', fontSize: '0.95rem', backgroundColor: isSelected ? 'var(--ralsei-black)' : '#8c8c8c' }}>
                                                        {formatDiscountBadge(voucher)}
                                                    </div>
                                                    <div className="p-2 flex-grow-1 position-relative">
                                                        <div className="fw-bold text-dark mb-0" style={{ fontSize: '0.85rem' }}>{voucher.voucherCode}</div>
                                                        <div className="text-muted" style={{ fontSize: '0.75rem' }}>Đơn tối thiểu {voucher.minOrderValue.toLocaleString()}đ</div>
                                                        <OverlayTrigger placement="top" overlay={<Tooltip style={{ fontSize: '0.75rem' }}>HSD: {new Date(voucher.endEffectiveDate).toLocaleDateString('vi-VN')}</Tooltip>}>
                                                            <span className="position-absolute bg-transparent text-secondary" style={{ bottom: '8px', right: '8px', fontSize: '0.85rem' }}><BsInfoCircle /></span>
                                                        </OverlayTrigger>
                                                    </div>
                                                </div>
                                            );
                                        })}
                                    </div>

                                    {currentVoucherId && (
                                        <div className="text-end mb-3">
                                            <span className="text-danger fw-medium" style={{ cursor: 'pointer', fontSize: '0.8rem' }} onClick={handleClearVoucher}>
                                                Hủy chọn mã hiện tại ✕
                                            </span>
                                        </div>
                                    )}
                                </>
                            ) : (
                                <Alert 
                                    variant="warning" 
                                    className="d-flex align-items-center gap-3 rounded-4 border-0 shadow-sm py-3 px-4"
                                    style={{ backgroundColor: '#FFF9E6', color: '#8A6D3B' }}
                                >
                                    <BsExclamationTriangleFill className="text-warning fs-4 flex-shrink-0" />
                                    <div style={{ fontSize: '0.9rem', fontWeight: '500' }}>
                                        <span className="fw-bold text-dark">Đăng nhập</span> để được áp dụng mã giảm giá!
                                    </div>
                                </Alert>
                            )}

                            <hr className="my-3" style={{ borderColor: '#e0e0e0' }} />

                            {/* CHI TIẾT GIÁ */}
                            <div className="mb-3">
                                <div className="fw-bold mb-3 d-flex align-items-center gap-2" style={{ fontSize: '1.05rem', color: 'var(--ralsei-black)' }}>
                                    <BsCashStack size={18} /> Chi tiết thanh toán
                                </div>

                                {priceError && (
                                    <Alert variant="warning" className="py-2 px-3 mb-2 rounded-3 border-0" style={{ fontSize: '0.8rem' }}>
                                        {priceError}
                                    </Alert>
                                )}

                                {priceLoading ? (
                                    <div className="text-center py-3">
                                        <Spinner animation="border" size="sm" />
                                        <span className="ms-2 text-muted" style={{ fontSize: '0.85rem' }}>Đang tính giá...</span>
                                    </div>
                                ) : priceData ? (
                                    <div className="d-flex flex-column gap-2" style={{ fontSize: '0.85rem' }}>
                                        <div className="d-flex justify-content-between text-muted">
                                            <span>Giá vé ({seatCount} ghế × {formatCurrency(priceData.basePrice)})</span>
                                            <span>{formatCurrency(priceData.basePrice * seatCount)}</span>
                                        </div>

                                        {Number(priceData.baseSurcharge) > 0 && (
                                            <div className="d-flex justify-content-between text-muted">
                                                <span>Phụ phí chặng ({seatCount} ghế × {formatCurrency(priceData.baseSurcharge)})</span>
                                                <span>{formatCurrency(priceData.baseSurcharge * seatCount)}</span>
                                            </div>
                                        )}

                                        <div className="d-flex justify-content-between text-muted">
                                            <span>Tạm tính</span>
                                            <span>{formatCurrency(priceData.totalRawPrice)}</span>
                                        </div>

                                        <div className="d-flex justify-content-between text-success">
                                            <span>Ưu đãi</span>
                                            <span>-{formatCurrency(priceData.discountAmount)}</span>
                                        </div>

                                        <hr className="my-1" style={{ borderColor: '#e0e0e0' }} />

                                        <div className="d-flex justify-content-between fw-bold" style={{ fontSize: '1rem', color: 'var(--ralsei-black)' }}>
                                            <span>Tổng thanh toán</span>
                                            <span style={{ color: 'var(--ralsei-black)' }}>{formatCurrency(priceData.totalFinalPrice)}</span>
                                        </div>

                                        {!pickupStopId || !dropoffStopId ? (
                                            <div className="text-muted fst-italic" style={{ fontSize: '0.75rem' }}>
                                                * Chọn điểm đón/trả để cập nhật phụ phí chặng (nếu có)
                                            </div>
                                        ) : null}
                                    </div>
                                ) : (
                                    <div className="text-muted text-center py-2" style={{ fontSize: '0.85rem' }}>
                                        Chưa có thông tin giá
                                    </div>
                                )}
                            </div>

                            <div className="d-flex justify-content-center pt-2">
                                <button
                                    type="submit"
                                    className="fw-bold border-0 px-5 py-2 rounded-pill shadow-sm booking-btn-general"
                                    disabled={priceLoading || !priceData || submitting}
                                >
                                    {submitting ? 'Đang tạo mã thanh toán...' : priceLoading ? 'Đang cập nhật giá...' : 'Thanh toán'}
                                </button>
                            </div>
                        </Card>
                    </Col>
                </Row>
            </Form>

            {/* MODAL VOUCHER */}
            <Modal show={showVoucherModal} onHide={() => setShowVoucherModal(false)} centered size="md">
                <Modal.Header closeButton className="border-bottom-0 pb-0">
                    <Modal.Title className="fw-bold text-dark" style={{ fontSize: '1.1rem' }}>Danh sách mã giảm giá</Modal.Title>
                </Modal.Header>
                <Modal.Body style={{ maxHeight: '450px', overflowY: 'auto' }} className="pt-3">
                    <div className="d-flex flex-column gap-3">
                        {initData.vouchers?.map(voucher => (
                            <div key={voucher.voucherId} className={`d-flex border rounded-3 overflow-hidden align-items-center position-relative ${currentVoucherId == voucher.voucherId ? 'bg-light' : ''}`} style={{ borderColor: currentVoucherId == voucher.voucherId ? 'var(--ralsei-black)' : '#e0e0e0', borderWidth: currentVoucherId == voucher.voucherId ? '2px' : '1px' }}>
                                <div className="text-white fw-bold d-flex align-items-center justify-content-center p-3" style={{ minWidth: '80px', minHeight: '85px', fontSize: '1.1rem', backgroundColor: currentVoucherId == voucher.voucherId ? 'var(--ralsei-black)' : '#8c8c8c' }}>
                                    {formatDiscountBadge(voucher)}
                                </div>
                                <div className="p-3 flex-grow-1">
                                    <div className="fw-bold text-dark mb-1" style={{ fontSize: '0.95rem' }}>{voucher.voucherCode}</div>
                                    <div className="text-muted mb-1" style={{ fontSize: '0.8rem' }}>Đơn tối thiểu: {voucher.minOrderValue.toLocaleString()}đ</div>
                                    <div className="text-muted mb-1" style={{ fontSize: '0.8rem' }}>Giảm tối đa: {formatCurrency(voucher.maxDiscountValue)}</div>
                                    <div className='text-danger mb-1' style={{ fontSize: '0.8rem' }}>Hạn sử dụng: {formatDateTime(voucher.endEffectiveDate)}</div>
                                </div>
                                <Button size="sm" className="position-absolute end-0 bottom-0 m-3 rounded-pill px-3 fw-medium" style={{ backgroundColor: currentVoucherId == voucher.voucherId ? 'var(--ralsei-black)' : 'transparent', color: currentVoucherId == voucher.voucherId ? '#fff' : 'var(--ralsei-black)', borderColor: 'var(--ralsei-black)', fontSize: '0.8rem' }} 
                                    onClick={() => { handleSelectVoucher(voucher); setShowVoucherModal(false); }}>
                                    {currentVoucherId == voucher.voucherId ? "Đang chọn" : "Chọn"}
                                </Button>
                            </div>
                        ))}
                    </div>
                </Modal.Body>
            </Modal>

            <PhoneOtpModal
                phone={otpPhone}
                show={Boolean(otpPhone)}
                onVerified={({ idToken }) => handleOtpVerified(otpPhone, idToken)}
                onClose={closeOtpModal}
            />
        </div>
    );
}
