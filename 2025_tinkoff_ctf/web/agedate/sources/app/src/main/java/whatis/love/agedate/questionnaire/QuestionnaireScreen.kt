package whatis.love.agedate.questionnaire

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import whatis.love.agedate.api.model.BirthDateVisibility
import whatis.love.agedate.user.data.QuestionnaireResult
import whatis.love.agedate.user.viewmodel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QuestionnaireScreen(
    userViewModel: UserViewModel,
    isFirstScreenBackable: Boolean = true,
    populateFromViewModel: Boolean = false,
    onBack: () -> Unit = {},
    onSuccess: () -> Unit = {},
    onComplete: (QuestionnaireState) -> Unit,
) {
    val user = userViewModel.userProfile.collectAsState()
    val questionnaireResult by userViewModel.questionnaireState.collectAsState()
    val questionnaireErrors: QuestionnaireResult.Error? =
        when (questionnaireResult) {
            is QuestionnaireResult.Error -> questionnaireResult as QuestionnaireResult.Error
            else -> null
        }
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    val enterTransition =
        remember { fadeIn() }
    val exitTransition =
        remember {
            shrinkVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            ) + fadeOut()
        }

    LaunchedEffect(Unit) {
        userViewModel.resetQuestionnaireState()
    }

    if (questionnaireResult is QuestionnaireResult.Success) {
        onSuccess()
    }

    var state by rememberSaveable {
        mutableStateOf(
            QuestionnaireState(
                firstName = if (populateFromViewModel) user.value?.firstName ?: "" else "",
                lastName = if (populateFromViewModel) user.value?.lastName ?: "" else "",
                bio = if (populateFromViewModel) user.value?.bio ?: "" else "",
                birthDay = if (populateFromViewModel) user.value?.birthDay ?: 1 else 1,
                birthMonth = if (populateFromViewModel) user.value?.birthMonth ?: 1 else 1,
                birthYear = if (populateFromViewModel) user.value?.birthYear ?: 2015 else 2015,
                birthDateVisibility =
                    if (populateFromViewModel) {
                        user.value?.birthDateVisibility
                            ?: BirthDateVisibility.HIDE_NONE
                    } else {
                        BirthDateVisibility.HIDE_NONE
                    },
                currentStep = 0,
            ),
        )
    }
    val screenCount = 4
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val pagerState =
        rememberPagerState(
            initialPage = state.currentStep,
            pageCount = { screenCount },
        )
    LaunchedEffect(pagerState.currentPage) {
        state = state.copy(currentStep = pagerState.currentPage)
        focusManager.clearFocus()
        keyboardController?.hide()
    }
    LaunchedEffect(questionnaireErrors) {
        questionnaireErrors?.let { errors ->
            val firstErrorPage =
                when {
                    errors.firstNameError != null || errors.lastNameError != null -> 0
                    errors.dateError != null -> 1
                    errors.bioError != null -> 2
                    else -> null
                }

            firstErrorPage?.let {
                if (pagerState.currentPage != it) {
                    pagerState.animateScrollToPage(it)
                }
            }
        }
    }
    val scope = rememberCoroutineScope()

    fun back() {
        if (state.currentStep > 0) {
            scope.launch {
                pagerState.animateScrollToPage(state.currentStep - 1)
            }
        } else if (isFirstScreenBackable) {
            onBack()
        }
    }

    BackHandler {
        back()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Анкета") },
                navigationIcon = {
                    if (isFirstScreenBackable || state.currentStep > 0) {
                        IconButton(onClick = { back() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад",
                            )
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                ) {
                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = true,
                        beyondViewportPageCount = 0,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                        ) {
                            when (page) {
                                0 ->
                                    NameScreen(
                                        firstName = state.firstName,
                                        lastName = state.lastName,
                                        firstNameError = questionnaireErrors?.firstNameError,
                                        lastNameError = questionnaireErrors?.lastNameError,
                                        onNext = { first, last ->
                                            state = state.copy(firstName = first, lastName = last)
                                            scope.launch {
                                                pagerState.animateScrollToPage(page + 1)
                                            }
                                        },
                                    )

                                1 ->
                                    DateOfBirthScreen(
                                        day = state.birthDay,
                                        month = state.birthMonth,
                                        year = state.birthYear,
                                        dateError = questionnaireErrors?.dateError,
                                        onNext = {
                                            scope.launch {
                                                pagerState.animateScrollToPage(page + 1)
                                            }
                                        },
                                        onUpdate = { day, month, year, visibility ->
                                            state =
                                                state.copy(
                                                    birthDay = day,
                                                    birthMonth = month,
                                                    birthYear = year,
                                                    birthDateVisibility = visibility,
                                                )
                                        },
                                    )

                                2 ->
                                    BioScreen(
                                        bio = state.bio,
                                        bioError = questionnaireErrors?.bioError,
                                        onNext = { bio ->
                                            state = state.copy(bio = bio)
                                            scope.launch {
                                                pagerState.animateScrollToPage(page + 1)
                                            }
                                        },
                                    )

                                3 ->
                                    SubscriptionScreen(
                                        userViewModel = userViewModel,
                                        onNext = {
                                            scope.launch {
                                                onComplete(state)
                                            }
                                        },
                                    )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !imeVisible,
                enter = enterTransition,
                exit = exitTransition,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    for (i in 0 until screenCount) {
                        Box(
                            modifier =
                                Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(12.dp)
                                    .background(
                                        color =
                                            if (i == state.currentStep) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.primaryContainer
                                            },
                                        shape = CircleShape,
                                    ),
                        )
                    }
                }
            }
        }
    }
}
