package ua.polodarb.gmsflags.ui.screens.flagChangeScreen

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.polodarb.gmsflags.data.databases.local.enities.SavedFlags
import ua.polodarb.gmsflags.data.repo.GmsDBRepository
import ua.polodarb.gmsflags.data.repo.RoomDBRepository

class FlagChangeScreenViewModel(
    private val pkgName: String,
    private val repository: GmsDBRepository,
    private val roomRepository: RoomDBRepository
) : ViewModel() {

    private val _stateBoolean =
        MutableStateFlow<FlagChangeUiStates>(FlagChangeUiStates.Loading)
    val stateBoolean: StateFlow<FlagChangeUiStates> = _stateBoolean.asStateFlow()

    private val _stateInteger =
        MutableStateFlow<FlagChangeUiStates>(FlagChangeUiStates.Loading)
    val stateInteger: StateFlow<FlagChangeUiStates> = _stateInteger.asStateFlow()

    private val _stateFloat =
        MutableStateFlow<FlagChangeUiStates>(FlagChangeUiStates.Loading)
    val stateFloat: StateFlow<FlagChangeUiStates> = _stateFloat.asStateFlow()

    private val _stateString =
        MutableStateFlow<FlagChangeUiStates>(FlagChangeUiStates.Loading)
    val stateString: StateFlow<FlagChangeUiStates> = _stateString.asStateFlow()

    private val _stateSavedFlags =
        MutableStateFlow<List<SavedFlags>>(emptyList())
    val stateSavedFlags: StateFlow<List<SavedFlags>> = _stateSavedFlags.asStateFlow()

    // Filter
    var filterMethod = mutableStateOf(FilterMethod.ALL)

    fun updateBoolFlagValue(flagName: String, newValue: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val currentState = _stateBoolean.value
                if (currentState is FlagChangeUiStates.Success) {
                    val updatedData = currentState.data.toMutableMap()
                    updatedData[flagName] = newValue
                    _stateBoolean.value = currentState.copy(data = updatedData)
                    listBoolFiltered.replace(flagName, newValue)
                }
            }
        }
    }

    fun turnOnAllBoolFlags() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val currentState = _stateBoolean.value
                if (currentState is FlagChangeUiStates.Success) {
                    val updatedData = currentState.data.mapValues { "1" }.toMutableMap()
                    _stateBoolean.value = currentState.copy(data = updatedData)
                    listBoolFiltered.replaceAll { _, _ -> "1" }
                }
            }
        }
    }

    fun turnOffAllBoolFlags() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val currentState = _stateBoolean.value
                if (currentState is FlagChangeUiStates.Success) {
                    val updatedData = currentState.data.mapValues { "0" }.toMutableMap()
                    _stateBoolean.value = currentState.copy(data = updatedData)
                    listBoolFiltered.replaceAll { _, _ -> "0" }
                }
            }
        }
    }

    fun updateIntFlagValue(flagName: String, newValue: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val currentState = _stateInteger.value
                if (currentState is FlagChangeUiStates.Success) {
                    val updatedData = currentState.data.toMutableMap()
                    updatedData[flagName] = newValue
                    _stateInteger.value = currentState.copy(data = updatedData)
                    listIntFiltered.replace(flagName, newValue)
                }
            }
        }
    }

    fun updateFloatFlagValue(flagName: String, newValue: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val currentState = _stateFloat.value
                if (currentState is FlagChangeUiStates.Success) {
                    val updatedData = currentState.data.toMutableMap()
                    updatedData[flagName] = newValue
                    _stateFloat.value = currentState.copy(data = updatedData)
                    listFloatFiltered.replace(flagName, newValue)
                }
            }
        }
    }

    fun updateStringFlagValue(flagName: String, newValue: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val currentState = _stateString.value
                if (currentState is FlagChangeUiStates.Success) {
                    val updatedData = currentState.data.toMutableMap()
                    updatedData[flagName] = newValue
                    _stateString.value = currentState.copy(data = updatedData)
                    listStringFiltered.replace(flagName, newValue)
                }
            }
        }

    }

    fun Map<String, String>.filterByEnabled(): Map<String, String> {
        val filteredMap = mutableMapOf<String, String>()
        for ((key, value) in this) {
            if (value == "1") {
                filteredMap[key] = value
            }
        }
        return filteredMap
    }

    fun Map<String, String>.filterByDisabled(): Map<String, String> {
        val filteredMap = mutableMapOf<String, String>()
        for ((key, value) in this) {
            if (value == "0") {
                filteredMap[key] = value
            }
        }
        return filteredMap
    }

    fun getAndroidPackage(pkgName: String): String {
        return repository.androidPackage(pkgName)
    }

    private val changedFilterBoolList = mutableMapOf<String, String>()
    private val usersList = mutableListOf<String>()

    // Search
    var searchQuery = mutableStateOf("")

    private val listBoolFiltered: MutableMap<String, String> = mutableMapOf()
    private val listIntFiltered: MutableMap<String, String> = mutableMapOf()
    private val listFloatFiltered: MutableMap<String, String> = mutableMapOf()
    private val listStringFiltered: MutableMap<String, String> = mutableMapOf()

    init {
        usersList.addAll(repository.getUsers())
        getAllSavedFlags()
        initBoolValues()
        initIntValues()
        initFloatValues()
        initStringValues()
    }

    fun initOverriddenBoolFlags(pkgName: String, delay: Boolean = false) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val data = repository.getOverriddenBoolFlagsByPackage(pkgName)
                when (data) {
                    is FlagChangeUiStates.Success -> {
                        changedFilterBoolList.clear()
                        changedFilterBoolList.putAll(data.data)
                        listBoolFiltered.putAll(data.data)
                    }

                    is FlagChangeUiStates.Loading -> {
                        _stateBoolean.value = FlagChangeUiStates.Loading
                    }

                    is FlagChangeUiStates.Error -> {
                        _stateBoolean.value = FlagChangeUiStates.Error()
                    }
                }
            }
        }
    }

    // Boolean
    fun initBoolValues(delay: Boolean = true) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.getBoolFlags(pkgName, delay).collect { uiStates ->
                    when (uiStates) {
                        is FlagChangeUiStates.Success -> {
                            listBoolFiltered.putAll(uiStates.data)
                            getBoolFlags()
                        }

                        is FlagChangeUiStates.Loading -> {
                            _stateBoolean.value = FlagChangeUiStates.Loading
                        }

                        is FlagChangeUiStates.Error -> {
                            _stateBoolean.value = FlagChangeUiStates.Error()
                        }
                    }
                }
            }
        }
    }

    fun getBoolFlags() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                when (filterMethod.value) {
                    FilterMethod.ENABLED -> {
                        _stateBoolean.value = FlagChangeUiStates.Success(
                            (listBoolFiltered.toMap().filterByEnabled()).filter {
                                it.key.contains(searchQuery.value, ignoreCase = true)
                            }
                        )
                    }

                    FilterMethod.DISABLED -> {
                        _stateBoolean.value = FlagChangeUiStates.Success(
                            (listBoolFiltered.toMap().filterByDisabled()).filter {
                                it.key.contains(searchQuery.value, ignoreCase = true)
                            }
                        )
                    }

                    FilterMethod.CHANGED -> {
                        _stateBoolean.value = FlagChangeUiStates.Success(
                            changedFilterBoolList.filter {
                                it.key.contains(searchQuery.value, ignoreCase = true)
                            }
                        )
                    }

                    else -> {
                        _stateBoolean.value = FlagChangeUiStates.Success(
                            listBoolFiltered.filter {
                                it.key.contains(searchQuery.value, ignoreCase = true)
                            }
                        )
                    }
                }
            }
        }
    }

    // Integer
    fun initIntValues(delay: Boolean = true) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.getIntFlags(pkgName, delay).collect { uiStates ->
                    when (uiStates) {
                        is FlagChangeUiStates.Success -> {
                            listIntFiltered.putAll(uiStates.data)
                            getIntFlags()
                        }

                        is FlagChangeUiStates.Loading -> {
                            _stateInteger.value = FlagChangeUiStates.Loading
                        }

                        is FlagChangeUiStates.Error -> {
                            _stateInteger.value = FlagChangeUiStates.Error()
                        }
                    }
                }
            }
        }
    }

    fun getIntFlags() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _stateInteger.value = FlagChangeUiStates.Success(
                    listIntFiltered.filter {
                        it.key.contains(searchQuery.value, ignoreCase = true)
                    }
                )
            }
        }
    }

    // Float
    fun initFloatValues(delay: Boolean = true) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.getFloatFlags(pkgName, delay).collect { uiStates ->
                    when (uiStates) {
                        is FlagChangeUiStates.Success -> {
                            listFloatFiltered.putAll(uiStates.data)
                            getFloatFlags()
                        }

                        is FlagChangeUiStates.Loading -> {
                            _stateFloat.value = FlagChangeUiStates.Loading
                        }

                        is FlagChangeUiStates.Error -> {
                            _stateInteger.value = FlagChangeUiStates.Error()
                        }
                    }
                }
            }
        }
    }

    fun getFloatFlags() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _stateFloat.value = FlagChangeUiStates.Success(
                    listFloatFiltered.filter {
                        it.key.contains(searchQuery.value, ignoreCase = true)
                    }
                )
            }
        }
    }

    // String
    fun initStringValues(delay: Boolean = true) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.getStringFlags(pkgName, delay).collect { uiStates ->
                    when (uiStates) {
                        is FlagChangeUiStates.Success -> {
                            listStringFiltered.putAll(uiStates.data)
                            getStringFlags()
                        }

                        is FlagChangeUiStates.Loading -> {
                            _stateString.value = FlagChangeUiStates.Loading
                        }

                        is FlagChangeUiStates.Error -> {
                            _stateInteger.value = FlagChangeUiStates.Error()
                        }
                    }
                }
            }
        }
    }

    fun getStringFlags() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _stateString.value = FlagChangeUiStates.Success(
                    listStringFiltered.filter {
                        it.key.contains(searchQuery.value, ignoreCase = true)
                    }
                )
            }
        }
    }

    fun clearPhenotypeCache(pkgName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val androidPkgName = repository.androidPackage(pkgName)
                Shell.cmd("am force-stop $androidPkgName").exec()
                Shell.cmd("rm -rf /data/data/$androidPkgName/files/phenotype").exec()
                if (pkgName.contains("finsky") || pkgName.contains("vending")) {
                    Shell.cmd("rm -rf /data/data/com.android.vending/files/experiment*").exec()
                    Shell.cmd("am force-stop com.android.vending").exec()
                }
                repeat(3) {
                    Shell.cmd("am start -a android.intent.action.MAIN -n $androidPkgName &").exec()
                    Shell.cmd("am force-stop $androidPkgName").exec()
                }
            }
        }
    }

    // Override Flag
    fun overrideFlag(
        packageName: String,
        name: String,
        flagType: Int = 0,
        intVal: String? = null,
        boolVal: String? = null,
        floatVal: String? = null,
        stringVal: String? = null,
        extensionVal: String? = null,
        committed: Int = 0
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.deleteRowByFlagName(packageName, name)
                repository.overrideFlag(
                    packageName = packageName,
                    user = "",
                    name = name,
                    flagType = flagType,
                    intVal = intVal,
                    boolVal = boolVal,
                    floatVal = floatVal,
                    stringVal = stringVal,
                    extensionVal = extensionVal,
                    committed = committed
                )
                for (i in usersList) {
                    repository.overrideFlag(
                        packageName = packageName,
                        user = i,
                        name = name,
                        flagType = flagType,
                        intVal = intVal,
                        boolVal = boolVal,
                        floatVal = floatVal,
                        stringVal = stringVal,
                        extensionVal = extensionVal,
                        committed = committed
                    )
                }
                clearPhenotypeCache(pkgName)
            }
        }
    }

    fun overrideAllFlag() { // todo
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                listBoolFiltered.forEach {
                    if (it.value == "0") {
                        overrideFlag(
                            packageName = pkgName,
                            it.key,
                            boolVal = "1"
                        )
                    }
                }
                turnOnAllBoolFlags()
            }
        }
    }

    // Delete overridden flags
    fun deleteOverriddenFlagByPackage(packageName: String) {
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
                repository.deleteOverriddenFlagByPackage(packageName)
//            }
//        }
    }

    // Saved flags

    private fun getAllSavedFlags() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                roomRepository.getSavedFlags().collect {
                    _stateSavedFlags.value = it
                }
            }
        }
    }

    fun saveFlag(flagName: String, pkgName: String, flagType: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                roomRepository.saveFlag(flagName, pkgName, flagType)
            }
        }
    }

    fun deleteSavedFlag(flagName: String, pkgName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                roomRepository.deleteSavedFlag(flagName, pkgName)
            }
        }
    }

}

enum class FilterMethod : MutableState<FilterMethod> {
    ALL, ENABLED, DISABLED, CHANGED;

    override var value: FilterMethod
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun component1(): FilterMethod {
        TODO("Not yet implemented")
    }

    override fun component2(): (FilterMethod) -> Unit {
        TODO("Not yet implemented")
    }
}