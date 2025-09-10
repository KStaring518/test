package com.example.shop.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(exclude = {"parent", "children"})
@ToString(exclude = {"parent", "children"})
@Entity
@Table(name = "categories")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Category {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String name;

	@ManyToOne
	@JoinColumn(name = "parent_id")
	@JsonBackReference
	private Category parent;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
	@JsonManagedReference
	private List<Category> children;

	@Column(name = "sort_order")
	private Integer sortOrder = 0;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CategoryStatus status = CategoryStatus.ENABLED;

	@CreationTimestamp
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	public enum CategoryStatus {
		ENABLED, DISABLED
	}
}


