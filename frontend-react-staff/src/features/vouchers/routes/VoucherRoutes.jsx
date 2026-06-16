import { Route } from 'react-router-dom';
import VoucherListPage from '../../../pages/manager/vouchers/VoucherListPage';
import CreateVoucherPage from '../../../pages/manager/vouchers/CreateVoucherPage';
import EditVoucherPage from '../../../pages/manager/vouchers/EditVoucherPage';

export const voucherRoutes = (
    <Route key="vouchers-group" path="vouchers">
        <Route index element={<VoucherListPage />} />
        <Route path="create" element={<CreateVoucherPage />} />
        <Route path=":id/edit" element={<EditVoucherPage />} />
    </Route>
);
