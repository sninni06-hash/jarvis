package com.example.planner

import com.example.planner.models.TaskIntent
import com.example.planner.models.TaskPlan
import com.example.planner.models.TaskStep
import java.util.UUID

object TaskPlanner {

    fun plan(goal: String): TaskPlan {
        val trimmed = goal.trim()
        val planId = UUID.randomUUID().toString()

        // Check if query contains compound conjunctions like "aur", "and", "bhi", comma, or semicolon
        val clauses = splitCompoundClauses(trimmed)

        val steps = if (clauses.size > 1) {
            clauses.mapIndexed { index, clause ->
                val intent = CommandRouter.parseIntent(clause)
                TaskStep(
                    stepNumber = index + 1,
                    totalSteps = clauses.size,
                    title = "Task ${index + 1}/${clauses.size}: ${intent.type.name.replace("_", " ")}",
                    description = clause.trim(),
                    intent = intent
                )
            }
        } else {
            val intent = CommandRouter.parseIntent(trimmed)
            listOf(
                TaskStep(
                    stepNumber = 1,
                    totalSteps = 1,
                    title = intent.type.name.replace("_", " "),
                    description = trimmed,
                    intent = intent
                )
            )
        }

        return TaskPlan(
            planId = planId,
            userGoal = goal,
            steps = steps
        )
    }

    private fun splitCompoundClauses(goal: String): List<String> {
        // Look for split points: " aur ", " and ", " also ", " comma ", " fir ", " then "
        val splitRegex = Regex("(?i)\\b(?:aur|and|fir|then|bhi)\\b|[,;]")
        val rawParts = goal.split(splitRegex)
            .map { it.trim() }
            .filter { it.length > 3 }

        return if (rawParts.size > 1) rawParts else listOf(goal)
    }
}
