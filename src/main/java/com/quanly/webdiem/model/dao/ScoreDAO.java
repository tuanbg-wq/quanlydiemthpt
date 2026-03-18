package com.quanly.webdiem.model.dao;

import com.quanly.webdiem.model.entity.Score;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface ScoreDAO extends JpaRepository<Score, Integer> {

    @Query(value = """
            SELECT
                s.id_hoc_sinh AS idHocSinh,
                COALESCE(NULLIF(TRIM(st.ho_ten), ''), s.id_hoc_sinh) AS tenHocSinh,
                COALESCE(NULLIF(TRIM(c.ten_lop), ''), COALESCE(NULLIF(TRIM(st.id_lop), ''), '-')) AS tenLop,
                s.id_mon_hoc AS idMonHoc,
                COALESCE(NULLIF(TRIM(sb.ten_mon_hoc), ''), s.id_mon_hoc) AS tenMonHoc,
                ROUND(AVG(CASE WHEN s.id_loai_diem = 4 THEN s.diem END), 1) AS diemGiuaKy,
                ROUND(AVG(CASE WHEN s.id_loai_diem = 5 THEN s.diem END), 1) AS diemCuoiKy,
                ROUND(
                    COALESCE(
                        MAX(av.dtb_mon),
                        SUM(s.diem * COALESCE(stt.he_so, 1)) / NULLIF(SUM(COALESCE(stt.he_so, 1)), 0)
                    ),
                    1
                ) AS tongKet,
                COALESCE(
                    NULLIF(TRIM(MAX(CASE WHEN cd.hoc_ky = s.hoc_ky THEN cd.xep_loai END)), ''),
                    NULLIF(TRIM(MAX(CASE WHEN cd.hoc_ky = 0 THEN cd.xep_loai END)), ''),
                    '-'
                ) AS hanhKiem,
                s.hoc_ky AS hocKy,
                s.nam_hoc AS namHoc
            FROM scores s
            LEFT JOIN score_types stt ON stt.id_loai_diem = s.id_loai_diem
            LEFT JOIN students st ON LOWER(st.id_hoc_sinh) = LOWER(s.id_hoc_sinh)
            LEFT JOIN classes c ON LOWER(c.id_lop) = LOWER(st.id_lop)
            LEFT JOIN subjects sb ON LOWER(sb.id_mon_hoc) = LOWER(s.id_mon_hoc)
            LEFT JOIN average_scores av
                ON LOWER(av.id_hoc_sinh) = LOWER(s.id_hoc_sinh)
               AND LOWER(av.id_mon_hoc) = LOWER(s.id_mon_hoc)
               AND av.nam_hoc = s.nam_hoc
               AND av.hoc_ky = s.hoc_ky
            LEFT JOIN conducts cd
                ON LOWER(cd.id_hoc_sinh) = LOWER(s.id_hoc_sinh)
               AND cd.nam_hoc = s.nam_hoc
            WHERE (
                    :q IS NULL OR :q = '' OR
                    LOWER(st.ho_ten) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(s.id_hoc_sinh) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(sb.ten_mon_hoc) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(s.id_mon_hoc) LIKE CONCAT('%', LOWER(:q), '%')
                )
              AND (
                    :khoi IS NULL OR
                    c.khoi = :khoi
                )
              AND (
                    :classId IS NULL OR :classId = '' OR
                    LOWER(c.id_lop) = LOWER(:classId)
                )
              AND (
                    :subjectId IS NULL OR :subjectId = '' OR
                    LOWER(s.id_mon_hoc) = LOWER(:subjectId)
                )
              AND (
                    :hocKy IS NULL OR
                    :hocKy = 0 OR
                    s.hoc_ky = :hocKy
                )
              AND (
                    :courseId IS NULL OR :courseId = '' OR
                    LOWER(c.id_khoa) = LOWER(:courseId)
                )
            GROUP BY
                s.id_hoc_sinh,
                st.ho_ten,
                c.khoi,
                c.ten_lop,
                st.id_lop,
                s.id_mon_hoc,
                sb.ten_mon_hoc,
                s.hoc_ky,
                s.nam_hoc
            ORDER BY
                c.khoi ASC,
                c.ten_lop ASC,
                st.ho_ten ASC,
                s.id_hoc_sinh ASC,
                sb.ten_mon_hoc ASC,
                s.hoc_ky ASC,
                s.nam_hoc DESC
            """, nativeQuery = true)
    List<Object[]> searchForManagement(@Param("q") String q,
                                       @Param("khoi") Integer khoi,
                                       @Param("classId") String classId,
                                       @Param("subjectId") String subjectId,
                                       @Param("hocKy") Integer hocKy,
                                       @Param("courseId") String courseId);

    @Query(value = """
            SELECT DISTINCT c.khoi
            FROM scores s
            JOIN students st ON LOWER(st.id_hoc_sinh) = LOWER(s.id_hoc_sinh)
            JOIN classes c ON LOWER(c.id_lop) = LOWER(st.id_lop)
            WHERE c.khoi IS NOT NULL
            ORDER BY c.khoi ASC
            """, nativeQuery = true)
    List<Integer> findDistinctGrades();

    @Query(value = """
            SELECT
                c.id_lop AS idLop,
                COALESCE(NULLIF(TRIM(c.ten_lop), ''), c.id_lop) AS tenLop,
                c.khoi AS khoi
            FROM scores s
            JOIN students st ON LOWER(st.id_hoc_sinh) = LOWER(s.id_hoc_sinh)
            JOIN classes c ON LOWER(c.id_lop) = LOWER(st.id_lop)
            GROUP BY c.id_lop, c.ten_lop, c.khoi
            ORDER BY c.khoi ASC, c.ten_lop ASC
            """, nativeQuery = true)
    List<Object[]> findDistinctClassesForFilter();

    @Query(value = """
            SELECT
                sb.id_mon_hoc AS idMonHoc,
                COALESCE(NULLIF(TRIM(sb.ten_mon_hoc), ''), sb.id_mon_hoc) AS tenMonHoc
            FROM scores s
            JOIN subjects sb ON LOWER(sb.id_mon_hoc) = LOWER(s.id_mon_hoc)
            GROUP BY sb.id_mon_hoc, sb.ten_mon_hoc
            ORDER BY sb.ten_mon_hoc ASC, sb.id_mon_hoc ASC
            """, nativeQuery = true)
    List<Object[]> findDistinctSubjectsForFilter();

    @Query(value = """
            SELECT
                c.id_khoa AS idKhoa,
                COALESCE(NULLIF(TRIM(k.ten_khoa), ''), c.id_khoa) AS tenKhoa
            FROM scores s
            JOIN students st ON LOWER(st.id_hoc_sinh) = LOWER(s.id_hoc_sinh)
            JOIN classes c ON LOWER(c.id_lop) = LOWER(st.id_lop)
            LEFT JOIN courses k ON LOWER(k.id_khoa) = LOWER(c.id_khoa)
            WHERE c.id_khoa IS NOT NULL
              AND TRIM(c.id_khoa) <> ''
            GROUP BY c.id_khoa, k.ten_khoa
            ORDER BY c.id_khoa ASC
            """, nativeQuery = true)
    List<Object[]> findDistinctCoursesForFilter();

    @Query(value = """
            SELECT COUNT(DISTINCT s.id_hoc_sinh)
            FROM scores s
            """, nativeQuery = true)
    long countDistinctStudentsWithScores();

    @Query(value = """
            SELECT ROUND(AVG(s.diem), 1)
            FROM scores s
            """, nativeQuery = true)
    Double calculateSchoolAverage();

    @Query(value = """
            SELECT COUNT(*)
            FROM (
                SELECT
                    s.id_hoc_sinh,
                    s.id_mon_hoc,
                    s.nam_hoc,
                    s.hoc_ky
                FROM scores s
                GROUP BY s.id_hoc_sinh, s.id_mon_hoc, s.nam_hoc, s.hoc_ky
            ) grouped_scores
            """, nativeQuery = true)
    long countScoreGroups();

    @Query(value = """
            SELECT COUNT(*)
            FROM (
                SELECT
                    SUM(s.diem * COALESCE(stt.he_so, 1)) / NULLIF(SUM(COALESCE(stt.he_so, 1)), 0) AS tongKet
                FROM scores s
                LEFT JOIN score_types stt ON stt.id_loai_diem = s.id_loai_diem
                GROUP BY s.id_hoc_sinh, s.id_mon_hoc, s.nam_hoc, s.hoc_ky
                HAVING tongKet >= 6.5
            ) grouped_scores
            """, nativeQuery = true)
    long countGoodScoreGroups();

    @Query(value = """
            SELECT
                s.id_hoc_sinh AS idHocSinh,
                COALESCE(NULLIF(TRIM(st.ho_ten), ''), s.id_hoc_sinh) AS tenHocSinh,
                s.id_mon_hoc AS idMonHoc,
                COALESCE(NULLIF(TRIM(sb.ten_mon_hoc), ''), s.id_mon_hoc) AS tenMonHoc,
                COALESCE(NULLIF(TRIM(c.ten_lop), ''), COALESCE(NULLIF(TRIM(st.id_lop), ''), '-')) AS tenLop,
                s.nam_hoc AS namHoc
            FROM scores s
            LEFT JOIN students st ON LOWER(st.id_hoc_sinh) = LOWER(s.id_hoc_sinh)
            LEFT JOIN classes c ON LOWER(c.id_lop) = LOWER(st.id_lop)
            LEFT JOIN subjects sb ON LOWER(sb.id_mon_hoc) = LOWER(s.id_mon_hoc)
            WHERE LOWER(s.id_hoc_sinh) = LOWER(:studentId)
              AND LOWER(s.id_mon_hoc) = LOWER(:subjectId)
              AND s.nam_hoc = :namHoc
            LIMIT 1
            """, nativeQuery = true)
    List<Object[]> findScoreGroupSummary(@Param("studentId") String studentId,
                                         @Param("subjectId") String subjectId,
                                         @Param("namHoc") String namHoc);

    @Query(value = """
            SELECT
                s.id_diem AS idDiem,
                s.hoc_ky AS hocKy,
                s.id_loai_diem AS idLoaiDiem,
                CASE
                    WHEN s.id_loai_diem = 4 THEN 'Giữa kỳ (HS2)'
                    WHEN s.id_loai_diem = 5 THEN 'Cuối kỳ (HS3)'
                    ELSE 'Đánh giá thường xuyên (HS1)'
                END AS tenLoaiDiem,
                s.diem AS diem,
                COALESCE(DATE_FORMAT(s.ngay_nhap, '%d/%m/%Y'), '') AS ngayNhap,
                COALESCE(NULLIF(TRIM(s.ghi_chu), ''), '') AS ghiChu
            FROM scores s
            LEFT JOIN score_types stt ON stt.id_loai_diem = s.id_loai_diem
            WHERE LOWER(s.id_hoc_sinh) = LOWER(:studentId)
              AND LOWER(s.id_mon_hoc) = LOWER(:subjectId)
              AND s.nam_hoc = :namHoc
            ORDER BY s.hoc_ky ASC, s.id_loai_diem ASC, s.ngay_nhap ASC, s.id_diem ASC
            """, nativeQuery = true)
    List<Object[]> findScoreEntriesByGroup(@Param("studentId") String studentId,
                                           @Param("subjectId") String subjectId,
                                           @Param("namHoc") String namHoc);

    @Query("""
            SELECT s
            FROM Score s
            WHERE LOWER(s.idHocSinh) = LOWER(:studentId)
              AND LOWER(s.idMonHoc) = LOWER(:subjectId)
              AND s.namHoc = :namHoc
            ORDER BY s.hocKy ASC, s.idLoaiDiem ASC, s.idDiem ASC
            """)
    List<Score> findScoresForEdit(@Param("studentId") String studentId,
                                  @Param("subjectId") String subjectId,
                                  @Param("namHoc") String namHoc);

    @Transactional
    long deleteByIdHocSinhIgnoreCaseAndIdMonHocIgnoreCaseAndNamHoc(String idHocSinh,
                                                                    String idMonHoc,
                                                                    String namHoc);

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM average_scores
            WHERE LOWER(id_hoc_sinh) = LOWER(:studentId)
              AND LOWER(id_mon_hoc) = LOWER(:subjectId)
              AND nam_hoc = :namHoc
            """, nativeQuery = true)
    int deleteAverageScoresByGroup(@Param("studentId") String studentId,
                                   @Param("subjectId") String subjectId,
                                   @Param("namHoc") String namHoc);

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM average_scores
            WHERE LOWER(id_hoc_sinh) = LOWER(:studentId)
              AND LOWER(id_mon_hoc) = LOWER(:subjectId)
              AND nam_hoc = :namHoc
              AND hoc_ky = :hocKy
            """, nativeQuery = true)
    int deleteAverageScoresByGroupAndSemester(@Param("studentId") String studentId,
                                              @Param("subjectId") String subjectId,
                                              @Param("namHoc") String namHoc,
                                              @Param("hocKy") Integer hocKy);

    @Query(value = """
            SELECT sy.nam_hoc
            FROM school_years sy
            ORDER BY sy.ngay_bat_dau DESC
            """, nativeQuery = true)
    List<String> findSchoolYearsForCreate();

    @Query(value = """
            SELECT
                c.id_khoa AS idKhoa,
                COALESCE(NULLIF(TRIM(k.ten_khoa), ''), c.id_khoa) AS tenKhoa
            FROM classes c
            LEFT JOIN courses k ON LOWER(k.id_khoa) = LOWER(c.id_khoa)
            WHERE c.id_khoa IS NOT NULL
              AND TRIM(c.id_khoa) <> ''
            GROUP BY c.id_khoa, k.ten_khoa
            ORDER BY c.id_khoa ASC
            """, nativeQuery = true)
    List<Object[]> findCoursesForCreate();

    @Query(value = """
            SELECT DISTINCT c.khoi
            FROM classes c
            WHERE c.khoi IS NOT NULL
            ORDER BY c.khoi ASC
            """, nativeQuery = true)
    List<Integer> findGradesForCreate();

    @Query(value = """
            SELECT
                c.id_lop AS idLop,
                COALESCE(NULLIF(TRIM(c.ten_lop), ''), c.id_lop) AS tenLop
            FROM classes c
            LEFT JOIN courses k ON LOWER(k.id_khoa) = LOWER(c.id_khoa)
            WHERE (:grade IS NULL OR c.khoi = :grade)
              AND (
                    :courseId IS NULL OR :courseId = '' OR
                    c.id_khoa = :courseId OR
                    c.id_khoa LIKE CONCAT('%', :courseId, '%') OR
                    COALESCE(k.ten_khoa, '') LIKE CONCAT('%', :courseId, '%')
                )
              AND (
                    :namHoc IS NULL OR :namHoc = '' OR
                    c.nam_hoc = :namHoc OR
                    COALESCE(c.nam_hoc, '') LIKE CONCAT('%', :namHoc, '%')
                )
            ORDER BY c.khoi ASC, c.ten_lop ASC, c.id_lop ASC
            """, nativeQuery = true)
    List<Object[]> findClassesForCreate(@Param("grade") Integer grade,
                                        @Param("courseId") String courseId,
                                        @Param("namHoc") String namHoc);

    @Query(value = """
            SELECT
                sb.id_mon_hoc AS idMonHoc,
                COALESCE(NULLIF(TRIM(sb.ten_mon_hoc), ''), sb.id_mon_hoc) AS tenMonHoc
            FROM subjects sb
            WHERE (:grade IS NULL OR :grade IS NOT NULL)
            ORDER BY sb.ten_mon_hoc ASC, sb.id_mon_hoc ASC
            """, nativeQuery = true)
    List<Object[]> findSubjectsForCreate(@Param("grade") Integer grade);

    @Query(value = """
            SELECT
                sb.id_mon_hoc AS idMonHoc,
                COALESCE(NULLIF(TRIM(sb.ten_mon_hoc), ''), sb.id_mon_hoc) AS tenMonHoc
            FROM subjects sb
            ORDER BY sb.ten_mon_hoc ASC, sb.id_mon_hoc ASC
            """, nativeQuery = true)
    List<Object[]> findAllSubjectsForCreate();

    @Query(value = """
            SELECT COALESCE(NULLIF(TRIM(sb.ten_mon_hoc), ''), sb.id_mon_hoc)
            FROM subjects sb
            WHERE LOWER(sb.id_mon_hoc) = LOWER(:subjectId)
            LIMIT 1
            """, nativeQuery = true)
    String findSubjectNameById(@Param("subjectId") String subjectId);

    @Query(value = """
            SELECT
                st.id_hoc_sinh AS idHocSinh,
                COALESCE(NULLIF(TRIM(st.ho_ten), ''), st.id_hoc_sinh) AS hoTen,
                COALESCE(NULLIF(TRIM(c.ten_lop), ''), COALESCE(NULLIF(TRIM(st.id_lop), ''), '-')) AS tenLop,
                COALESCE(NULLIF(TRIM(st.id_lop), ''), '') AS idLop,
                COALESCE(CAST(c.khoi AS CHAR), '') AS khoi,
                COALESCE(NULLIF(TRIM(c.id_khoa), ''), '') AS idKhoa
            FROM students st
            LEFT JOIN classes c ON LOWER(c.id_lop) = LOWER(st.id_lop)
            WHERE (:classId IS NULL OR :classId = '' OR LOWER(st.id_lop) = LOWER(:classId))
              AND (
                    :q IS NULL OR :q = '' OR
                    LOWER(st.id_hoc_sinh) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(st.ho_ten) LIKE CONCAT('%', LOWER(:q), '%')
                )
            ORDER BY st.ho_ten ASC, st.id_hoc_sinh ASC
            LIMIT 120
            """, nativeQuery = true)
    List<Object[]> findStudentsForCreate(@Param("classId") String classId,
                                         @Param("q") String q);

    @Query(value = """
            SELECT
                st.id_hoc_sinh AS idHocSinh,
                COALESCE(NULLIF(TRIM(st.ho_ten), ''), st.id_hoc_sinh) AS hoTen,
                COALESCE(NULLIF(TRIM(c.ten_lop), ''), COALESCE(NULLIF(TRIM(st.id_lop), ''), '-')) AS tenLop,
                COALESCE(NULLIF(TRIM(st.id_lop), ''), '') AS idLop,
                COALESCE(CAST(c.khoi AS CHAR), '') AS khoi,
                COALESCE(NULLIF(TRIM(c.id_khoa), ''), '') AS idKhoa
            FROM students st
            LEFT JOIN classes c ON LOWER(c.id_lop) = LOWER(st.id_lop)
            WHERE LOWER(st.id_hoc_sinh) = LOWER(:studentId)
            LIMIT 1
            """, nativeQuery = true)
    List<Object[]> findStudentForCreateById(@Param("studentId") String studentId);

    @Query(value = """
            SELECT t.id_giao_vien
            FROM teachers t
            JOIN users u ON u.id_tai_khoan = t.id_tai_khoan
            WHERE LOWER(u.ten_dang_nhap) = LOWER(:username)
            LIMIT 1
            """, nativeQuery = true)
    String findTeacherIdByUsername(@Param("username") String username);

    @Query(value = """
            SELECT
                t.id_giao_vien AS idGiaoVien,
                COALESCE(NULLIF(TRIM(t.ho_ten), ''), t.id_giao_vien) AS hoTen
            FROM teachers t
            WHERE (
                    EXISTS (
                        SELECT 1
                        FROM teaching_assignments ta
                        WHERE LOWER(ta.id_giao_vien) = LOWER(t.id_giao_vien)
                          AND (:subjectId IS NULL OR :subjectId = '' OR LOWER(ta.id_mon_hoc) = LOWER(:subjectId))
                          AND (:classId IS NULL OR :classId = '' OR LOWER(ta.id_lop) = LOWER(:classId))
                          AND (:namHoc IS NULL OR :namHoc = '' OR ta.nam_hoc = :namHoc)
                          AND (:hocKy IS NULL OR ta.hoc_ky = :hocKy)
                    )
                    OR (
                        :subjectId IS NOT NULL AND :subjectId <> ''
                        AND EXISTS (
                            SELECT 1
                            FROM subjects s
                            WHERE LOWER(s.id_mon_hoc) = LOWER(:subjectId)
                              AND LOWER(COALESCE(s.id_giao_vien_phu_trach, '')) = LOWER(t.id_giao_vien)
                        )
                    )
                  )
              AND (
                    :q IS NULL OR :q = '' OR
                    LOWER(t.id_giao_vien) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(t.ho_ten) LIKE CONCAT('%', LOWER(:q), '%')
                  )
            GROUP BY t.id_giao_vien, t.ho_ten
            ORDER BY t.ho_ten ASC, t.id_giao_vien ASC
            LIMIT 20
            """, nativeQuery = true)
    List<Object[]> suggestTeachingTeachersForScore(@Param("subjectId") String subjectId,
                                                   @Param("classId") String classId,
                                                   @Param("namHoc") String namHoc,
                                                   @Param("hocKy") Integer hocKy,
                                                   @Param("q") String q);

    @Query(value = """
            SELECT DISTINCT
                t.id_giao_vien AS idGiaoVien,
                COALESCE(NULLIF(TRIM(t.ho_ten), ''), t.id_giao_vien) AS hoTen
            FROM teachers t
            WHERE EXISTS (
                    SELECT 1
                    FROM subjects s
                    WHERE LOWER(s.id_mon_hoc) = LOWER(:subjectId)
                      AND (
                            LOWER(COALESCE(s.id_giao_vien_phu_trach, '')) = LOWER(t.id_giao_vien)
                            OR LOWER(TRIM(COALESCE(s.ten_mon_hoc, ''))) = LOWER(TRIM(COALESCE(t.chuyen_mon, '')))
                            OR EXISTS (
                                SELECT 1
                                FROM teaching_assignments ta
                                WHERE LOWER(ta.id_mon_hoc) = LOWER(s.id_mon_hoc)
                                  AND LOWER(ta.id_giao_vien) = LOWER(t.id_giao_vien)
                            )
                          )
                )
              AND (
                    :q IS NULL OR :q = '' OR
                    LOWER(t.id_giao_vien) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(t.ho_ten) LIKE CONCAT('%', LOWER(:q), '%')
                  )
            ORDER BY t.ho_ten ASC, t.id_giao_vien ASC
            LIMIT 20
            """, nativeQuery = true)
    List<Object[]> suggestTeachersBySubjectForScore(@Param("subjectId") String subjectId,
                                                    @Param("q") String q);

    @Query(value = """
            SELECT
                t.id_giao_vien AS idGiaoVien,
                COALESCE(NULLIF(TRIM(t.ho_ten), ''), t.id_giao_vien) AS hoTen
            FROM teachers t
            WHERE (
                    :q IS NULL OR :q = '' OR
                    LOWER(t.id_giao_vien) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(t.ho_ten) LIKE CONCAT('%', LOWER(:q), '%')
                  )
            ORDER BY t.ho_ten ASC, t.id_giao_vien ASC
            LIMIT 20
            """, nativeQuery = true)
    List<Object[]> suggestAllTeachersForScore(@Param("q") String q);

    @Query(value = """
            SELECT ta.id_giao_vien
            FROM teaching_assignments ta
            WHERE LOWER(ta.id_mon_hoc) = LOWER(:subjectId)
              AND LOWER(ta.id_lop) = LOWER(:classId)
              AND ta.nam_hoc = :namHoc
              AND ta.hoc_ky = :hocKy
            ORDER BY ta.id_giao_vien ASC
            LIMIT 1
            """, nativeQuery = true)
    String findFirstAssignedTeacherForScore(@Param("subjectId") String subjectId,
                                            @Param("classId") String classId,
                                            @Param("namHoc") String namHoc,
                                            @Param("hocKy") Integer hocKy);

    @Query(value = """
            SELECT COALESCE(NULLIF(TRIM(t.ho_ten), ''), t.id_giao_vien)
            FROM teachers t
            WHERE LOWER(t.id_giao_vien) = LOWER(:teacherId)
            LIMIT 1
            """, nativeQuery = true)
    String findTeacherNameById(@Param("teacherId") String teacherId);

    @Query(value = """
            SELECT
                c.id_khoa AS idKhoa,
                COALESCE(NULLIF(TRIM(k.ten_khoa), ''), c.id_khoa) AS tenKhoa
            FROM classes c
            LEFT JOIN courses k ON LOWER(k.id_khoa) = LOWER(c.id_khoa)
            WHERE c.id_khoa IS NOT NULL
              AND TRIM(c.id_khoa) <> ''
              AND (
                    :q IS NULL OR :q = '' OR
                    LOWER(c.id_khoa) LIKE CONCAT('%', LOWER(:q), '%') OR
                    LOWER(COALESCE(k.ten_khoa, '')) LIKE CONCAT('%', LOWER(:q), '%')
                )
            GROUP BY c.id_khoa, k.ten_khoa
            ORDER BY c.id_khoa ASC
            LIMIT 15
            """, nativeQuery = true)
    List<Object[]> findCourseSuggestionsForCreate(@Param("q") String q);

    @Query(value = """
            SELECT
                s.hoc_ky AS hocKy,
                s.id_loai_diem AS idLoaiDiem,
                s.diem AS diem,
                COALESCE(NULLIF(TRIM(s.ghi_chu), ''), '') AS ghiChu,
                COALESCE(NULLIF(TRIM(s.id_giao_vien), ''), '') AS idGiaoVien
            FROM scores s
            WHERE LOWER(s.id_hoc_sinh) = LOWER(:studentId)
              AND LOWER(s.id_mon_hoc) = LOWER(:subjectId)
              AND s.nam_hoc = :namHoc
            ORDER BY s.hoc_ky ASC, s.id_loai_diem ASC, s.id_diem ASC
            """, nativeQuery = true)
    List<Object[]> findRawScoreEntriesForCreate(@Param("studentId") String studentId,
                                                @Param("subjectId") String subjectId,
                                                @Param("namHoc") String namHoc);

    @Query(value = """
            SELECT
                c.hoc_ky AS hocKy,
                COALESCE(NULLIF(TRIM(c.xep_loai), ''), '') AS xepLoai
            FROM conducts c
            WHERE LOWER(c.id_hoc_sinh) = LOWER(:studentId)
              AND c.nam_hoc = :namHoc
            ORDER BY c.hoc_ky ASC
            """, nativeQuery = true)
    List<Object[]> findConductsForCreate(@Param("studentId") String studentId,
                                         @Param("namHoc") String namHoc);

    @Query(value = """
            SELECT
                st.id_loai_diem AS idLoaiDiem,
                COALESCE(st.he_so, 1) AS heSo,
                COALESCE(NULLIF(TRIM(st.ten_loai), ''), '') AS tenLoai
            FROM score_types st
            ORDER BY st.id_loai_diem ASC
            """, nativeQuery = true)
    List<Object[]> findScoreTypeDefinitions();

    @Query(value = """
            SELECT COUNT(*)
            FROM teaching_assignments ta
            WHERE LOWER(ta.id_giao_vien) = LOWER(:teacherId)
              AND LOWER(ta.id_mon_hoc) = LOWER(:subjectId)
              AND ta.nam_hoc = :namHoc
              AND ta.hoc_ky = :hocKy
              AND LOWER(ta.id_lop) = LOWER(:classId)
            """, nativeQuery = true)
    long countTeachingAssignmentForScore(@Param("teacherId") String teacherId,
                                         @Param("subjectId") String subjectId,
                                         @Param("namHoc") String namHoc,
                                         @Param("hocKy") Integer hocKy,
                                         @Param("classId") String classId);

    @Modifying
    @Transactional
    @Query(value = "SET @app_is_admin = :isAdmin", nativeQuery = true)
    int setAdminBypassFlag(@Param("isAdmin") Integer isAdmin);

    @Modifying
    @Transactional
    @Query(value = "SET @app_is_admin = NULL", nativeQuery = true)
    int clearAdminBypassFlag();

    @Modifying
    @Transactional
    @Query(value = """
            INSERT IGNORE INTO teaching_assignments
                (id_giao_vien, id_mon_hoc, id_lop, nam_hoc, hoc_ky, ngay_bat_dau)
            VALUES
                (:teacherId, :subjectId, :classId, :namHoc, :hocKy, CURRENT_DATE())
            """, nativeQuery = true)
    int ensureTeachingAssignmentForScore(@Param("teacherId") String teacherId,
                                         @Param("subjectId") String subjectId,
                                         @Param("classId") String classId,
                                         @Param("namHoc") String namHoc,
                                         @Param("hocKy") Integer hocKy);

    @Modifying
    @Transactional
    @Query(value = """
            DELETE FROM scores
            WHERE LOWER(id_hoc_sinh) = LOWER(:studentId)
              AND LOWER(id_mon_hoc) = LOWER(:subjectId)
              AND nam_hoc = :namHoc
              AND hoc_ky = :hocKy
            """, nativeQuery = true)
    int deleteScoresByGroupAndSemester(@Param("studentId") String studentId,
                                       @Param("subjectId") String subjectId,
                                       @Param("namHoc") String namHoc,
                                       @Param("hocKy") Integer hocKy);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO scores
                (id_hoc_sinh, id_mon_hoc, id_loai_diem, nam_hoc, hoc_ky, diem, id_giao_vien, ngay_nhap, ghi_chu)
            VALUES
                (:studentId, :subjectId, :scoreTypeId, :namHoc, :hocKy, :scoreValue, :teacherId, CURRENT_DATE(), :note)
            """, nativeQuery = true)
    int insertScoreEntry(@Param("studentId") String studentId,
                         @Param("subjectId") String subjectId,
                         @Param("scoreTypeId") Integer scoreTypeId,
                         @Param("namHoc") String namHoc,
                         @Param("hocKy") Integer hocKy,
                         @Param("scoreValue") BigDecimal scoreValue,
                         @Param("teacherId") String teacherId,
                         @Param("note") String note);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO conducts
                (id_hoc_sinh, nam_hoc, hoc_ky, xep_loai, nhan_xet, id_gvcn, ngay_cap_nhat)
            VALUES
                (:studentId, :namHoc, :hocKy, :xepLoai, :nhanXet, :teacherId, CURRENT_TIMESTAMP())
            ON DUPLICATE KEY UPDATE
                xep_loai = VALUES(xep_loai),
                nhan_xet = VALUES(nhan_xet),
                id_gvcn = VALUES(id_gvcn),
                ngay_cap_nhat = CURRENT_TIMESTAMP()
            """, nativeQuery = true)
    int upsertConduct(@Param("studentId") String studentId,
                      @Param("namHoc") String namHoc,
                      @Param("hocKy") Integer hocKy,
                      @Param("xepLoai") String xepLoai,
                      @Param("nhanXet") String nhanXet,
                      @Param("teacherId") String teacherId);
}
