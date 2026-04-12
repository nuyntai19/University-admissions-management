package vn.edu.sgu.phanmemtuyensinh.dal;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import vn.edu.sgu.phanmemtuyensinh.dal.entity.ToHopMon;
import vn.edu.sgu.phanmemtuyensinh.utils.HibernateUtil;

public class ToHopMonDAO {

    // 1. Lấy danh sách tất cả tổ hợp môn (Xem danh sách)
    public List<ToHopMon> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Câu lệnh HQL (Hibernate Query Language) query trên Entity thay vì Table
            return session.createQuery("FROM ToHopMon", ToHopMon.class).list();
        }
    }

    public ToHopMon getByMaToHop(String maToHop) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM ToHopMon WHERE maToHop = :ma", ToHopMon.class)
                    .setParameter("ma", maToHop)
                    .uniqueResult();
        }
    }

    // 2. Thêm mới một tổ hợp
    public boolean add(ToHopMon toHop) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(toHop); // persist là lệnh thêm mới
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    // 3. Cập nhật thông tin (Sửa)
    public boolean update(ToHopMon toHop) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(toHop); // merge là lệnh cập nhật
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }

    // 4. Xóa tổ hợp theo ID
    public boolean delete(int idToHop) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            ToHopMon toHop = session.get(ToHopMon.class, idToHop);
            if (toHop != null) {
                session.remove(toHop); // remove là lệnh xóa
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            e.printStackTrace();
            return false;
        }
    }
}