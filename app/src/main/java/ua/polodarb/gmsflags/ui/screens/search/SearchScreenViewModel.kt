package ua.polodarb.gmsflags.ui.screens.search

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ua.polodarb.gmsflags.data.AppInfo
import ua.polodarb.gmsflags.data.constants.SortingTypeConstants.APP_NAME
import ua.polodarb.gmsflags.data.constants.SortingTypeConstants.APP_NAME_REVERSED
import ua.polodarb.gmsflags.data.constants.SortingTypeConstants.LAST_UPDATE
import ua.polodarb.gmsflags.data.constants.SortingTypeConstants.PACKAGE_NAME
import ua.polodarb.gmsflags.data.repo.AppsListRepository
import ua.polodarb.gmsflags.data.repo.GmsDBRepository
import ua.polodarb.gmsflags.data.repo.RoomDBRepository
import ua.polodarb.gmsflags.data.repo.interactors.GmsDBInteractor
import ua.polodarb.gmsflags.data.repo.mappers.FlagDetails
import ua.polodarb.gmsflags.data.repo.mappers.MergeFlagsMapper
import ua.polodarb.gmsflags.data.repo.mappers.MergedAllTypesFlags
import ua.polodarb.repository.uiStates.UiStates
import java.util.Collections

typealias AppInfoList = ua.polodarb.repository.uiStates.UiStates<PersistentList<AppInfo>>
typealias AppDialogList = ua.polodarb.repository.uiStates.UiStates<PersistentList<String>>
typealias PackagesScreenUiStates = ua.polodarb.repository.uiStates.UiStates<Map<String, String>>
typealias AllFlagsScreenUiStates = ua.polodarb.repository.uiStates.UiStates<List<FlagDetails>>

class SearchScreenViewModel(
    private val repository: AppsListRepository,
    private val gmsRepository: GmsDBRepository,
    private val roomRepository: RoomDBRepository,
    private val mergeFlagsMapper: MergeFlagsMapper,
    private val gmsDBInteractor: GmsDBInteractor
) : ViewModel() {

    // Apps List
    private val _appsListUiState =
        MutableStateFlow<AppInfoList>(ua.polodarb.repository.uiStates.UiStates.Loading())
    val appsListUiState: StateFlow<AppInfoList> = _appsListUiState.asStateFlow()

    private val _dialogDataState =
        MutableStateFlow<AppDialogList>(ua.polodarb.repository.uiStates.UiStates.Loading())
    val dialogDataState: StateFlow<AppDialogList> = _dialogDataState.asStateFlow()

    private val _dialogPackage = MutableStateFlow("")
    val dialogPackage: StateFlow<String> = _dialogPackage.asStateFlow()


    // Packages List
    private val _packagesListUiState = MutableStateFlow<PackagesScreenUiStates>(ua.polodarb.repository.uiStates.UiStates.Loading())
    val packagesListUiState: StateFlow<PackagesScreenUiStates> = _packagesListUiState.asStateFlow()

    private val _stateSavedPackages =
        MutableStateFlow<List<String>>(emptyList())
    val stateSavedPackages: StateFlow<List<String>> = _stateSavedPackages.asStateFlow()

    val selectedFlagsTypeChip = mutableStateOf(0)

    // All Flags List // TODO

    private val _allFlagsUiState =
        MutableStateFlow<ua.polodarb.repository.uiStates.UiStates<MergedAllTypesFlags>>(ua.polodarb.repository.uiStates.UiStates.Loading())
    val allFlagsUiState: StateFlow<ua.polodarb.repository.uiStates.UiStates<MergedAllTypesFlags>> = _allFlagsUiState.asStateFlow()

    private val _allFlagsBoolUiState = MutableStateFlow<AllFlagsScreenUiStates>(ua.polodarb.repository.uiStates.UiStates.Loading())
    val allFlagsBoolUiState: StateFlow<AllFlagsScreenUiStates> = _allFlagsBoolUiState.asStateFlow()

    private val _allFlagsIntUiState = MutableStateFlow<AllFlagsScreenUiStates>(ua.polodarb.repository.uiStates.UiStates.Loading())
    val allFlagsIntUiState: StateFlow<AllFlagsScreenUiStates> = _allFlagsIntUiState.asStateFlow()

    private val _allFlagsFloatUiState = MutableStateFlow<AllFlagsScreenUiStates>(ua.polodarb.repository.uiStates.UiStates.Loading())
    val allFlagsFloatUiState: StateFlow<AllFlagsScreenUiStates> =
        _allFlagsFloatUiState.asStateFlow()

    private val _allFlagsStringUiState =
        MutableStateFlow<AllFlagsScreenUiStates>(ua.polodarb.repository.uiStates.UiStates.Loading())
    val allFlagsStringUiState: StateFlow<AllFlagsScreenUiStates> =
        _allFlagsStringUiState.asStateFlow()

    // Search and filter
    var appsSearchQuery = mutableStateOf("")
    private val appsListFiltered: MutableList<AppInfo> = mutableListOf()

    var packagesSearchQuery = mutableStateOf("")
    private val packagesListFiltered: MutableMap<String, String> = mutableMapOf()

    var allFlagsSearchQuery = mutableStateOf("")
    private var allFlagsListFiltered: MergedAllTypesFlags = MergedAllTypesFlags(
        emptyList(),
        emptyList(),
        emptyList(),
        emptyList()
    )

    private val usersList = Collections.synchronizedList(mutableListOf<String>())

    fun clearSearchQuery() {
        appsSearchQuery.value = ""
        packagesSearchQuery.value = ""
        allFlagsSearchQuery.value = ""
    }

    // Sorting
    val sortType = mapOf(
        APP_NAME to "By app name",
        APP_NAME_REVERSED to "By app name (Reversed)",
        LAST_UPDATE to "By last update",
        PACKAGE_NAME to "By package name"
    )

    var setSortType = mutableStateOf(sortType[APP_NAME])

    init {
        initUsers()
        initGms()
    }

    fun initGms() {
        initAllInstalledApps()
        initGmsPackagesList()
        getAllSavedPackages()
//        initAllFlags()
    }

    private fun initUsers() {
        viewModelScope.launch {
            gmsRepository.getUsers().collect {
                usersList.addAll(it)
            }
        }
    }

    fun setPackageToDialog(pkgName: String) {
        _dialogPackage.value = pkgName
    }

    fun setEmptyList() {
        _dialogDataState.value = ua.polodarb.repository.uiStates.UiStates.Success(persistentListOf())
    }

    /**
     * **AppsListScreen** - get list of packages in app
     */
    fun getListByPackages(pkgName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.getListByPackages(pkgName).collect { uiStates ->
                    when (uiStates) {
                        is ua.polodarb.repository.uiStates.UiStates.Success -> {
                            _dialogDataState.value =
                                ua.polodarb.repository.uiStates.UiStates.Success(uiStates.data.toPersistentList())
                        }

                        is ua.polodarb.repository.uiStates.UiStates.Loading -> {
                            _dialogDataState.value = ua.polodarb.repository.uiStates.UiStates.Loading()
                        }

                        is ua.polodarb.repository.uiStates.UiStates.Error -> {
                            _dialogDataState.value = ua.polodarb.repository.uiStates.UiStates.Error()
                        }
                    }
                }
            }
        }
    }

    /**
     * **AppsListScreen** - init list of all installed apps
     */
    private fun initAllInstalledApps() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.getAllInstalledApps().collectLatest { uiStates ->
                    when (uiStates) {
                        is ua.polodarb.repository.uiStates.UiStates.Success -> {
                            appsListFiltered.addAll(uiStates.data)
                            getAllInstalledApps()
                        }

                        is ua.polodarb.repository.uiStates.UiStates.Loading -> {
                            _appsListUiState.value = ua.polodarb.repository.uiStates.UiStates.Loading()
                        }

                        is ua.polodarb.repository.uiStates.UiStates.Error -> {
                            _appsListUiState.value = ua.polodarb.repository.uiStates.UiStates.Error()
                        }
                    }
                }
            }
        }
    }

    /**
     * **AppsListScreen** - get list of all installed apps
     */
    fun getAllInstalledApps() {
        if (appsListFiltered.isNotEmpty()) {
            _appsListUiState.value = ua.polodarb.repository.uiStates.UiStates.Success(
                appsListFiltered.filter {
                    it.appName.contains(appsSearchQuery.value, ignoreCase = true)
                }.let { filteredList ->
                    when (setSortType.value) {
                        sortType[PACKAGE_NAME] -> filteredList.sortedBy { it.applicationInfo.packageName }
                        sortType[APP_NAME], sortType[APP_NAME_REVERSED] -> {
                            if (setSortType.value == sortType[APP_NAME_REVERSED]) {
                                filteredList.sortedByDescending { it.appName }
                            } else {
                                filteredList.sortedBy { it.appName }
                            }
                        }
                        sortType[LAST_UPDATE] -> {
                            filteredList.sortedByDescending {
                                if (it.packageInfo != null) {
                                    it.packageInfo.lastUpdateTime
                                } else {
                                    0L
                                }
                            }
                        }
                        else -> filteredList
                    }
                }.toPersistentList()
            )
        }
    }



    private fun initGmsPackagesList() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                gmsRepository.getGmsPackages().collect { uiState ->
                    when (uiState) {
                        is ua.polodarb.repository.uiStates.UiStates.Success -> {
                            packagesListFiltered.putAll(uiState.data)
                            getGmsPackagesList()
                        }

                        is ua.polodarb.repository.uiStates.UiStates.Loading -> {
                            _packagesListUiState.value = ua.polodarb.repository.uiStates.UiStates.Loading()
                        }

                        is ua.polodarb.repository.uiStates.UiStates.Error -> {
                            _packagesListUiState.value = ua.polodarb.repository.uiStates.UiStates.Error()
                        }
                    }
                }
            }
        }
    }

    fun getGmsPackagesList() {
        if (packagesListFiltered.isNotEmpty()) {
            _packagesListUiState.value = ua.polodarb.repository.uiStates.UiStates.Success(
                packagesListFiltered.filter {
                    it.key.contains(packagesSearchQuery.value, ignoreCase = true)
                }.toSortedMap()
            )
        }
    }

    private fun getAllSavedPackages() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                roomRepository.getSavedPackages().collect {
                    _stateSavedPackages.value = it
                }
            }
        }
    }

    fun savePackage(pkgName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                roomRepository.savePackage(pkgName)
            }
        }
    }

    fun deleteSavedPackage(pkgName: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                roomRepository.deleteSavedPackage(pkgName)
            }
        }
    }

    /**
     * **AllFlagsScreen** - init list of all flags apps
     */
//    private fun initAllFlags() {
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                mergeFlagsMapper.getMergedAllFlags().collectLatest { uiStates ->
//                    when (uiStates) {
//                        is UiStates.Success -> {
//                            Log.d("initAllFlags", "initAllFlags: ${uiStates.data}")
//                            allFlagsListFiltered = uiStates.data
//                            getAllFlags()
//                        }
//
//                        is UiStates.Loading -> {
//                            _appsListUiState.value = UiStates.Loading()
//                        }
//
//                        is UiStates.Error -> {
//                            _appsListUiState.value = UiStates.Error()
//                        }
//                    }
//                }
//            }
//        }
//    }

    /**
     * **AppsListScreen** - get list of all installed apps
     */
//    fun getAllFlags() {
//        if (allFlagsListFiltered.isNotEmpty()) {
//            _allFlagsUiState.value = UiStates.Success(
//                MergedAllTypesFlags(
//                    boolFlag = allFlagsListFiltered.boolFlag.filter {
//                        it.flagName.contains(appsSearchQuery.value, ignoreCase = true)
//                    },
//                    intFlag = emptyList(),
//                    floatFlag = emptyList(),
//                    stringFlag = emptyList()
//                    intFlag = allFlagsListFiltered.intFlag.filter {
//                        it.flagName.contains(appsSearchQuery.value, ignoreCase = true)
//                    },
//                    floatFlag = allFlagsListFiltered.floatFlag.filter {
//                        it.flagName.contains(appsSearchQuery.value, ignoreCase = true)
//                    },
//                    stringFlag = allFlagsListFiltered.stringFlag.filter {
//                        it.flagName.contains(appsSearchQuery.value, ignoreCase = true)
//                    }
//                )
//            )
//        }
//    }

    fun overrideFlag(
        packageName: String,
        name: String,
        flagType: Int = 0,
        intVal: String? = null,
        boolVal: String? = null,
        floatVal: String? = null,
        stringVal: String? = null,
        extensionVal: String? = null,
        committed: Int = 0,
        clearData: Boolean = true
    ) {
        viewModelScope.launch {
            gmsDBInteractor.overrideFlag(
                packageName = packageName,
                name = name,
                flagType = flagType,
                intVal = intVal,
                boolVal = boolVal,
                floatVal = floatVal,
                stringVal = stringVal,
                extensionVal = extensionVal,
                committed = committed,
                clearData = clearData,
                usersList = listOf("")
            )
        }
    }
}
