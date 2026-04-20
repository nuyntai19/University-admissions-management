package vn.edu.sgu.phanmemtuyensinh.tools;

import vn.edu.sgu.phanmemtuyensinh.bus.NganhBUS;

public class ClearNganh {

    public static void main(String[] args) {
        NganhBUS bus = new NganhBUS();

        System.out.println("=== TestClearNganh ===");
        System.out.println("Dang xoa...");

        boolean success = bus.clearAndResetId();

        if (success) {
            System.out.println("Ket qua: Thanh cong.");
            System.out.println("Bang xt_nganh da duoc xoa sach va id da duoc reset ve 1.");
        } else {
            System.out.println("Ket qua: That bai.");
            System.out.println("Vui long kiem tra log va a thong bao loi cua ung dung.");
        }
    }
}