package com.example.data.repository

import com.example.data.local.dao.EmployeeDao
import com.example.data.local.entity.EmployeeEntity
import com.example.data.local.entity.EmployeeWithDetails
import kotlinx.coroutines.flow.Flow

/**
 * Repository class for managing employee data.
 * مستودع إدارة بيانات الموظفين (إضافة، تحديث، أرشفة، حذف، وجلب البيانات).
 */
class EmployeeRepository(private val employeeDao: EmployeeDao) {

    fun getActiveEmployees(): Flow<List<EmployeeEntity>> = employeeDao.getActiveEmployees()

    fun getArchivedEmployees(): Flow<List<EmployeeEntity>> = employeeDao.getArchivedEmployees()

    fun getAllEmployees(): Flow<List<EmployeeEntity>> = employeeDao.getAllEmployees()

    suspend fun getEmployeeById(id: Long): EmployeeEntity? = employeeDao.getById(id)

    fun getEmployeeWithDetails(id: Long): Flow<EmployeeWithDetails?> = employeeDao.getEmployeeWithDetails(id)

    suspend fun insertEmployee(employee: EmployeeEntity): Long {
        return employeeDao.insert(employee)
    }

    suspend fun updateEmployee(employee: EmployeeEntity) {
        employeeDao.update(employee)
    }

    suspend fun archiveEmployee(id: Long) {
        employeeDao.archiveEmployee(id)
    }

    suspend fun deleteEmployee(employee: EmployeeEntity) {
        employeeDao.delete(employee)
    }
}
