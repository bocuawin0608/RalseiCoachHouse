import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Alert, Button, Card, Col, Row, Spinner } from 'react-bootstrap';
import { BsCheckCircleFill, BsClipboard, BsClock, BsExclamationTriangleFill } from 'react-icons/bs';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, useParams } from 'react-router-dom';
import { bookingApi } from '../api/bookingApi';
import { usePaymentSse } from '../hooks/usePaymentSse';
import { setPaymentInfo, setPaymentStatus } from '../reducers/bookingSlice';
import { formatCurrency, formatDateTime } from '../../../utils/formatters';
import { loadPaymentSession, mapPaymentPageResponse, savePaymentSession } from '../utils/paymentSession';

const PENDING = 'PENDING';
const COMPLETED = 'COMPLETED';
const FAILED = 'FAILED';

const getErrorMessage = (error) => {
    const responseData = error?.response?.data;
    if (responseData?.message) return responseData.message;
    if (typeof responseData === 'string') return responseData;
    if (responseData?.error) return responseData.error;
    return error?.message || 'Không thể tải thông tin thanh toán. Vui lòng thử lại!';
};

const formatCountdown = (seconds) => {
    if (seconds === null || seconds === undefined) return '--:--';
    const safeSeconds = Math.max(0, seconds);
    const minutes = Math.floor(safeSeconds / 60);
    const remainingSeconds = safeSeconds % 60;
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
};

const buildQrUrl = (paymentInfo) => {
    if (!paymentInfo?.bankName || !paymentInfo?.bankAccountNumber || !paymentInfo?.amount || !paymentInfo?.transactionId) {
        return '';
    }

    const params = new URLSearchParams({
        bank: paymentInfo.bankName,
        acc: paymentInfo.bankAccountNumber,
        template: 'compact',
        amount: String(paymentInfo.amount),
        des: paymentInfo.transactionId,
    });

    return `https://qr.sepay.vn/img?${params.toString()}`;
};

export default function Step3Payment() {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const { transactionId } = useParams();
    const storedPaymentInfo = useSelector((state) => state.booking.paymentInfo);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [copiedField, setCopiedField] = useState('');
    const [timeLeft, setTimeLeft] = useState(null);
    const hasRequestedExpireRef = useRef(false);

    const paymentInfo = storedPaymentInfo?.transactionId === transactionId ? storedPaymentInfo : null;
    const status = paymentInfo?.status || PENDING;
    const isCompleted = status === COMPLETED;
    const isFailed = status === FAILED;
    const isPending = status === PENDING;
    const isExpired = isPending && timeLeft === 0;
    const canPay = isPending && !isExpired;

    const seatCodes = useMemo(
        () => (Array.isArray(paymentInfo?.seatCodes) ? paymentInfo.seatCodes.join(', ') : ''),
        [paymentInfo?.seatCodes]
    );

    const qrUrl = useMemo(() => (canPay ? buildQrUrl(paymentInfo) : ''), [canPay, paymentInfo]);

    const applyPaymentStatus = useCallback((nextStatus) => {
        if (!transactionId || !nextStatus) return;
        dispatch(setPaymentStatus(nextStatus));
        const cached = loadPaymentSession(transactionId);
        if (cached) {
            savePaymentSession(transactionId, { ...cached, status: nextStatus });
        }
    }, [dispatch, transactionId]);

    useEffect(() => {
        if (!transactionId) {
            setError('Thiếu mã giao dịch thanh toán.');
            setLoading(false);
            return;
        }

        if (storedPaymentInfo?.transactionId === transactionId) {
            setLoading(false);
            return;
        }

        const cached = loadPaymentSession(transactionId);
        if (cached?.transactionId === transactionId) {
            dispatch(setPaymentInfo(cached));
            setLoading(false);
            return;
        }

        let cancelled = false;

        const fetchPaymentPage = async () => {
            setLoading(true);
            setError('');
            try {
                const response = await bookingApi.getPaymentPage(transactionId);
                const mapped = mapPaymentPageResponse(response);
                savePaymentSession(transactionId, mapped);
                if (!cancelled) {
                    dispatch(setPaymentInfo(mapped));
                }
            } catch (err) {
                if (!cancelled) {
                    setError(getErrorMessage(err));
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        };

        fetchPaymentPage();
        return () => {
            cancelled = true;
        };
    }, [dispatch, storedPaymentInfo?.transactionId, transactionId]);

    useEffect(() => {
        if (!paymentInfo?.paymentExpiresAt || !isPending) return;

        const tick = () => {
            const expiresAt = new Date(paymentInfo.paymentExpiresAt).getTime();
            const secondsLeft = Math.max(0, Math.ceil((expiresAt - Date.now()) / 1000));
            setTimeLeft(secondsLeft);
        };

        tick();
        const timer = setInterval(tick, 1000);
        return () => clearInterval(timer);
    }, [isPending, paymentInfo?.paymentExpiresAt]);

    useEffect(() => {
        if (!transactionId || !isExpired || !isPending || hasRequestedExpireRef.current) return;

        hasRequestedExpireRef.current = true;

        const expirePayment = async () => {
            try {
                const response = await bookingApi.expirePayment(transactionId);
                const mapped = mapPaymentPageResponse(response);
                dispatch(setPaymentInfo(mapped));
                savePaymentSession(transactionId, mapped);
            } catch (err) {
                console.error('Không thể hết hạn giao dịch thanh toán:', err);
            }
        };

        expirePayment();
    }, [dispatch, isExpired, isPending, transactionId]);

    useEffect(() => {
        if (isCompleted) {
            const timer = setTimeout(() => {
                navigate('/'); 
            }, 15000);

            return () => clearTimeout(timer); 
        }
    }, [isCompleted, navigate]);

    usePaymentSse(transactionId, {
        enabled: Boolean(transactionId && isPending && !isExpired),
        onStatusChange: applyPaymentStatus,
    });

    const handleCopy = async (field, value) => {
        if (!value) return;
        await navigator.clipboard.writeText(String(value));
        setCopiedField(field);
        setTimeout(() => setCopiedField(''), 1500);
    };

    if (loading) {
        return (
            <div className="text-center py-5">
                <Spinner animation="border" />
                <div className="fw-semibold mt-3">Đang tải thông tin thanh toán...</div>
            </div>
        );
    }

    if (error || !paymentInfo) {
        return (
            <Alert variant="danger" className="rounded-3 border-0 shadow-sm d-flex gap-2 align-items-start">
                <BsExclamationTriangleFill className="mt-1" />
                <div>{error || 'Không tìm thấy thông tin thanh toán cho mã giao dịch này.'}</div>
            </Alert>
        );
    }

    return (
        <div>

            {isCompleted && (
                <Alert variant="success" className="rounded-3 border-0 shadow-sm d-flex gap-2 align-items-center">
                    <BsCheckCircleFill />
                    <div>
                        Thanh toán thành công. Hệ thống đã xác nhận vé của bạn. 
                        <br />
                        <strong>Hệ thống sẽ tự động điều hướng sang trang chủ sau 15 giây...</strong>
                    </div>
                </Alert>
            )}

            {(isFailed || isExpired) && (
                <Alert variant="danger" className="rounded-3 border-0 shadow-sm d-flex gap-2 align-items-start">
                    <BsExclamationTriangleFill className="mt-1" />
                    <div>
                        Mã thanh toán đã hết hạn hoặc bị hủy. Ghế sẽ được giải phóng — vui lòng đặt lại vé nếu bạn chưa chuyển khoản.
                    </div>
                </Alert>
            )}

            <Row className="g-4">
                <Col lg={5}>
                    <Card className="border-0 rounded-4 shadow-sm h-100">
                        <Card.Body className="p-4 text-center">
                            {canPay && qrUrl ? (
                                <img
                                    src={qrUrl}
                                    alt="Mã QR thanh toán SePay"
                                    className="img-fluid rounded-3 border p-2 bg-white"
                                    style={{ maxWidth: '320px', width: '100%' }}
                                />
                            ) : (
                                <Alert variant={isCompleted ? 'success' : 'secondary'} className="text-start mb-0">
                                    {isCompleted
                                        ? 'Thanh toán đã hoàn tất. Mã QR không còn hiệu lực.'
                                        : 'Mã QR không còn hiệu lực. Vui lòng đặt lại vé nếu cần thanh toán mới.'}
                                </Alert>
                            )}

                            <div className="mt-3 fw-semibold" style={{ color: 'var(--ralsei-black)' }}>
                                {canPay ? 'Quét QR và bấm thanh toán trên ứng dụng ngân hàng' : 'Giao dịch đã kết thúc'}
                            </div>

                            {canPay && (
                                <div className="d-flex justify-content-center align-items-center gap-2 text-danger fw-bold mt-3">
                                    <BsClock />
                                    Còn lại {formatCountdown(timeLeft)}
                                </div>
                            )}
                        </Card.Body>
                    </Card>
                </Col>

                <Col lg={7}>
                    <Card className="border-0 rounded-4 shadow-sm h-100">
                        <Card.Body className="p-4">
                            <div className="fw-bold mb-3" style={{ fontSize: '1.05rem', color: 'var(--ralsei-black)' }}>
                                Thông tin chuyển khoản dự phòng
                            </div>

                            <div className="d-flex flex-column gap-3">
                                <TransferRow label="Ngân hàng" value={(isFailed || isExpired) ? 'N/A' : paymentInfo.bankName} field="bank" copiedField={copiedField} onCopy={handleCopy} disabled={!canPay} />
                                <TransferRow label="Số tài khoản" value={(isFailed || isExpired) ? 'N/A' : paymentInfo.bankAccountNumber} field="account" copiedField={copiedField} onCopy={handleCopy} disabled={!canPay} />
                                <TransferRow label="Số tiền" value={(isFailed || isExpired) ? 'N/A' : formatCurrency(paymentInfo.amount)} copyValue={paymentInfo.amount} field="amount" copiedField={copiedField} onCopy={handleCopy} disabled={!canPay} />
                                <TransferRow label="Nội dung" value={(isFailed || isExpired) ? 'N/A' : paymentInfo.transactionId} field="description" copiedField={copiedField} onCopy={handleCopy} disabled={!canPay} />
                            </div>

                            <hr className="my-4" />

                            <div className="fw-bold mb-3" style={{ fontSize: '1.05rem', color: 'var(--ralsei-black)' }}>
                                Tóm tắt vé
                            </div>

                            <div className="d-flex flex-column gap-2" style={{ fontSize: '0.9rem' }}>
                                <SummaryRow label="Mã vé" value={paymentInfo.ticketCode} />
                                <SummaryRow label="Hành khách đại diện" value={paymentInfo.primaryPassengerName || '---'} />
                                <SummaryRow label="Số điện thoại" value={paymentInfo.primaryPassengerPhone || '---'} />
                                <SummaryRow label="Ghế" value={seatCodes || '---'} />
                                <SummaryRow label="Hạn thanh toán" value={formatDateTime(paymentInfo.paymentExpiresAt)} />
                            </div>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </div>
    );
}

function TransferRow({ label, value, copyValue, field, copiedField, onCopy, disabled = false }) {
    return (
        <div className="d-flex justify-content-between align-items-center gap-3 border-bottom pb-2">
            <div>
                <div className="text-muted" style={{ fontSize: '0.8rem' }}>{label}</div>
                <div className={`fw-semibold ${disabled ? 'text-muted' : 'text-dark'}`}>{value || '---'}</div>
            </div>
            <Button
                type="button"
                variant="outline-dark"
                size="sm"
                className="rounded-pill d-flex align-items-center gap-1 px-3"
                onClick={() => onCopy(field, copyValue || value)}
                disabled={!value || disabled}
            >
                <BsClipboard size={14} />
                {copiedField === field ? 'Đã copy' : 'Copy'}
            </Button>
        </div>
    );
}

function SummaryRow({ label, value }) {
    return (
        <div className="d-flex justify-content-between gap-3">
            <span className="text-muted">{label}</span>
            <span className="fw-semibold text-end">{value}</span>
        </div>
    );
}
