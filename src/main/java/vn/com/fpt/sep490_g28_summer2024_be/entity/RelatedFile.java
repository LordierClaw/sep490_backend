package vn.com.fpt.sep490_g28_summer2024_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Entity
@Table(name = "related_file")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RelatedFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "related_file_id", columnDefinition = "BIGINT")
    private BigInteger relatedFileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(name = "file", columnDefinition = "VARCHAR(255)")
    private String file;

    @Override
    public String toString() {
        return "RelatedFile{" +
                "relatedFileId=" + relatedFileId +
                ", file='" + file + '\'' +
                '}';
    }
}
